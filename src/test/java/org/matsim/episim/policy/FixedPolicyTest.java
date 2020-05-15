package org.matsim.episim.policy;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.data.Offset;
import org.junit.Before;
import org.junit.Test;
import org.matsim.episim.EpisimTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class FixedPolicyTest {

	private static final Offset<Double> OFFSET = Offset.offset(0.0001);

	private ImmutableMap<String, Restriction> r;

	@Before
	public void setUp() {
		r = ImmutableMap.of(
				"home", Restriction.none(),
				"work", Restriction.none()
		);
	}

	@Test
	public void fixedDays() {

		FixedPolicy.ConfigBuilder config = FixedPolicy.config()
				.restrict(2, 0.3, "home");

		FixedPolicy policy = new FixedPolicy(config.build());

		policy.updateRestrictions(EpisimTestUtils.createReport("--", 2), r);

		assertThat(r.get("home").getRemainingFraction()).isEqualTo(0.3);

	}

	@Test
	public void interpolate() {

		FixedPolicy.ConfigBuilder config = FixedPolicy.config()
				.interpolate(
						LocalDate.of(2020, 3, 20),
						LocalDate.of(2020, 3, 30),
						Restriction.of(0.95), 0.45, "work"
				)
				.interpolate(
						LocalDate.of(2020, 3, 31),
						LocalDate.of(2020, 4, 10),
						Restriction.of(0.5), 1, "work"
				);

		FixedPolicy policy = new FixedPolicy(config.build());

		policy.updateRestrictions(EpisimTestUtils.createReport("2020-03-20", -1), r);
		assertThat(r.get("work").getRemainingFraction()).isEqualTo(0.95);

		// mid of first interval
		policy.updateRestrictions(EpisimTestUtils.createReport("2020-03-25", -1), r);
		assertThat(r.get("work").getRemainingFraction()).isCloseTo(0.95 - (0.95 - 0.45) / 2, OFFSET);

		policy.updateRestrictions(EpisimTestUtils.createReport("2020-03-30", -1), r);
		assertThat(r.get("work").getRemainingFraction()).isEqualTo(0.45);

		policy.updateRestrictions(EpisimTestUtils.createReport("2020-04-10", -1), r);
		assertThat(r.get("work").getRemainingFraction()).isCloseTo(1, OFFSET);


	}
}