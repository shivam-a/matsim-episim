package org.matsim.episim.model;

import com.google.inject.Inject;
import org.eclipse.collections.api.map.primitive.MutableIntBooleanMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntBooleanHashMap;
import org.matsim.episim.EpisimConfigGroup;
import org.matsim.episim.EpisimPerson;
import org.matsim.episim.policy.Restriction;

import java.util.SplittableRandom;

/**
 * Default model only requires people to wear the mask mandated by current {@link Restriction}.
 * Whether a person wears a mask is decided anew everyday.
 */
public class DefaultFaceMaskModel implements FaceMaskModel {

	private final MutableIntBooleanMap personWearsMask = new IntBooleanHashMap();
	private final EpisimConfigGroup episimConfig;
	private final SplittableRandom rnd;

	@Inject
	public DefaultFaceMaskModel(EpisimConfigGroup episimConfig, SplittableRandom rnd) {
		this.episimConfig = episimConfig;
		this.rnd = rnd;
	}


	@Override
	public void setIteration(int iteration) {
		// reset if person is wearing a mask everyday.
		personWearsMask.clear();
	}

	@Override
	public FaceMask getWornMask(EpisimPerson person, EpisimConfigGroup.InfectionParams act, int currentDay, Restriction restriction) {

		if (restriction.getRequireMask() == FaceMask.NONE) return FaceMask.NONE;

		// global parameter overwritten
		if (restriction.getComplianceRate() != null) {
			if (restriction.getComplianceRate() == 1d) return restriction.getRequireMask();
			if (restriction.getComplianceRate() == 0d) return FaceMask.NONE;
			return rnd.nextDouble() < restriction.getComplianceRate() ? restriction.getRequireMask() : FaceMask.NONE;
		}

		if (episimConfig.getMaskCompliance() == 1d) return restriction.getRequireMask();
		if (episimConfig.getMaskCompliance() == 0d) return FaceMask.NONE;

		int key = person.getPersonId().index();

		if (!personWearsMask.containsKey(key)) {
			personWearsMask.put(key, rnd.nextDouble() < episimConfig.getMaskCompliance());
		}

		return personWearsMask.get(key) ? restriction.getRequireMask() : FaceMask.NONE;
	}
}
