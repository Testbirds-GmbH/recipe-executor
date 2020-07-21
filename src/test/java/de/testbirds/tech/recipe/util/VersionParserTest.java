package de.testbirds.tech.recipe.util;/*
 * Copyright 2019 Testbirds GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Parameterized unit tests for the {@link VersionParser}, testing different version strings.
 *
 * @author testbirds
 */
@RunWith(Parameterized.class)
public class VersionParserTest {

	/**
	 * The unit under test.
	 */
	private final VersionParser unitUnderTest;

	/**
	 * The array of expected version indices that should be returned.
	 */
	private final List<Integer> versions;

	/**
	 * Constructor for the parameterized test case.
	 *
	 * @param version
	 *            the version string to parse.
	 * @param versions
	 *            the versions that should be returned.
	 */
	public VersionParserTest(final String version, final List<Integer> versions) {
		this.unitUnderTest = new VersionParser(version);
		this.versions = versions;
	}

	/**
	 * Test the if the unit under test properly parses the version. (This automatically tests getMajor)
	 */
	@Test
	public final void testGet() {
		for (int i = 0; i < versions.size(); i++) {
			assertThat(unitUnderTest.get(i), is(equalTo(versions.get(i))));
		}
	}

	/**
	 * Returns the data set for the parameterized runner.
	 *
	 * @return the data for the parametrized runner.
	 */
	@Parameters(name = "{index}: {0}")
	public static Collection<Object[]> data() {
		final List<Object[]> result = new ArrayList<Object[]>();
		result.add(new Object[] { null, Arrays.asList(0) });
		result.add(new Object[] { "", Arrays.asList(0) });
		result.add(new Object[] { "1", Arrays.asList(1) });
		result.add(new Object[] { "3", Arrays.asList(3) });
		result.add(new Object[] { "12a", Arrays.asList(12) });
		result.add(new Object[] { "a3", Arrays.asList(0) });
		result.add(new Object[] { "2.1", Arrays.asList(2, 1) });
		result.add(new Object[] { "summer release", Arrays.asList(0) });
		result.add(new Object[] { "10.11", Arrays.asList(10, 11) });
		result.add(new Object[] { "106.12.945.Hallo", Arrays.asList(106, 12, 945, 0) });
		result.add(new Object[] { "thisShouldFail.12.945.Hallo", Arrays.asList(0, 0, 0, 0) });
		result.add(new Object[] { "release.Test.Hallo", Arrays.asList(0, 0, 0) });

		return result;
	}
}
