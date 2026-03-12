package eu.ill.visa.core.domain;

import eu.ill.visa.core.entity.Flavour;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record FlavourUsage(Flavour flavour, Long usage) {

    static List<FlavourUsage> combine(List<FlavourUsage> first, List<FlavourUsage> second) {
        List<FlavourUsage> secondClone = new ArrayList<>(second);
        List<FlavourUsage> combinedFlavourUsages = first.stream()
            .map(usage -> {
                FlavourUsage toCombine = secondClone.stream().filter(secondUsage -> secondUsage.flavour().getId().equals(usage.flavour().getId())).findFirst().orElse(null);
                if (toCombine != null) {
                    secondClone.remove(toCombine);
                    return new FlavourUsage(usage.flavour(), usage.usage() + toCombine.usage());

                } else {
                    return new FlavourUsage(usage.flavour(), usage.usage());
                }
            })
            .collect(Collectors.toList());
        combinedFlavourUsages.addAll(secondClone);

        return combinedFlavourUsages;
    }

    static Long getFlavourUsage(List<FlavourUsage> flavourUsages, Flavour flavour) {
        return flavourUsages.stream().filter(usage -> usage.flavour.getId().equals(flavour.getId())).findFirst().map(FlavourUsage::usage).orElse(0L);
    }
}
