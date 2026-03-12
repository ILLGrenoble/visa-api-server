package eu.ill.visa.core.domain;


import eu.ill.visa.core.entity.Flavour;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record FlavourUsages(List<FlavourUsage> flavourUsages) {

    public record FlavourUsage(Flavour flavour, Long usage) {}

    public FlavourUsages(FlavourUsage usage) {
        this(List.of(usage));
    }

    public FlavourUsages combine(FlavourUsages other) {
        List<FlavourUsage> others = new ArrayList<>(other.flavourUsages);
        List<FlavourUsage> combinedFlavourUsages = flavourUsages.stream()
            .map(usage -> {
                FlavourUsage toCombine = others.stream().filter(secondUsage -> secondUsage.flavour().getId().equals(usage.flavour().getId())).findFirst().orElse(null);
                if (toCombine != null) {
                    others.remove(toCombine);
                    return new FlavourUsage(usage.flavour(), usage.usage() + toCombine.usage());

                } else {
                    return new FlavourUsage(usage.flavour(), usage.usage());
                }
            })
            .collect(Collectors.toList());
        combinedFlavourUsages.addAll(others);

        return new FlavourUsages(combinedFlavourUsages);
    }

    public Long getFlavourUsage(Flavour flavour) {
        return flavourUsages.stream().filter(usage -> usage.flavour.getId().equals(flavour.getId())).findFirst().map(FlavourUsage::usage).orElse(0L);
    }
}
