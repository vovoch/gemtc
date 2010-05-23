package org.drugis.mtc

import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.Assert._
import org.junit.Test

class ParametrizationTest extends ShouldMatchersForJUnit {
	@Test def testCycleClass() {
		val network = Network.noneFromXML(
			<network type="none">
				<treatments>
					<treatment id="A"/>
					<treatment id="B"/>
					<treatment id="C"/>
				</treatments>
				<studies>
					<study id="1">
						<measurement treatment="A" />
						<measurement treatment="B" />
					</study>
					<study id="2">
						<measurement treatment="A" />
						<measurement treatment="B" />
						<measurement treatment="C" />
					</study>
					<study id="3">
						<measurement treatment="B" />
						<measurement treatment="C" />
					</study>
				</studies>
			</network>)

		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")

		val st = new Tree[Treatment](Set[(Treatment, Treatment)](
			(a, b), (a, c)), a)

		val basis = new FundamentalGraphBasis(network.treatmentGraph, st)

		val p = new Parametrization(network, basis)

		val cycle = Cycle(List(a, b, c, a))

		val partition = new Partition(Set(
				new Part(a, b, Set(network.study("1"), network.study("2"))),
				new Part(a, c, Set(network.study("2"))),
				new Part(b, c, Set(network.study("2"), network.study("3")))
			))

		p.cycleClass(cycle) should be (Some((partition, 1)))

		p.inconsistencyClasses should be (Set(partition))
		p.inconsistencyCycles(partition) should be (Set(cycle))
		p.inconsistencyDegree should be (1)
	}

	val network = Network.noneFromXML(<network type="none">
		<treatments>
			<treatment id="A"/>
			<treatment id="B"/>
			<treatment id="C"/>
			<treatment id="D"/>
		</treatments>
		<studies>
			<study id="1">
				<measurement treatment="D" />
				<measurement treatment="B" />
				<measurement treatment="C" />
			</study>
			<study id="2">
				<measurement treatment="A" />
				<measurement treatment="B" />
			</study>
			<study id="3">
				<measurement treatment="A" />
				<measurement treatment="C" />
			</study>
			<study id="4">
				<measurement treatment="A" />
				<measurement treatment="D" />
			</study>
		</studies>
	</network>)

	@Test def testConsistencyClass() {
		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		// BCDB has support from only two studies
		val cycle = Cycle(List(b, c, d, b))
		val basis2 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, b), (b, d), (b, c)), a))
		new Parametrization(network, basis2).cycleClass(cycle) should be (None)
	}

	@Test def testNegativeSignClass() {
		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		// ADBCA reduces to ACDA
		val adbca = Cycle(List(a, d, b, c, a)) // -1
		val acda = Cycle(List(a, c, d, a)) // +1
		val basis3 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, c), (a, d), (d, b)), a))
		val param = new Parametrization(network, basis3)

		val partition = new Partition(Set(
				new Part(a, c, Set(network.study("3"))),
				new Part(c, d, Set(network.study("1"))),
				new Part(d, a, Set(network.study("4")))
			))

		param.cycleClass(acda) should be (Some((partition, 1)))
		param.cycleClass(adbca) should be (Some((partition, -1)))
	}

	@Test def testInconsistencyDegree() {
		val a = new Treatment("A")
		val b = new Treatment("B")
		val c = new Treatment("C")
		val d = new Treatment("D")

		// None of the cycles reduce, all of them have 3 distinct support
		val basis1 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, b), (a, c), (a, d)), a))
		new Parametrization(network, basis1).inconsistencyDegree should be (3)

		// BCDB has support from only two studies
		val basis2 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, b), (b, d), (b, c)), a))
		new Parametrization(network, basis2).inconsistencyDegree should be (2)

		// ACBDA reduces to ACDA
		val basis3 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, c), (a, d), (d, b)), a))
		new Parametrization(network, basis3).inconsistencyDegree should be (2)

		// DBCD has support from only one study
		val basis4 = new FundamentalGraphBasis(network.treatmentGraph,
			new Tree[Treatment](Set[(Treatment, Treatment)](
				(a, d), (d, c), (d, b)), a))
		new Parametrization(network, basis4).inconsistencyDegree should be (2)
	}
}
