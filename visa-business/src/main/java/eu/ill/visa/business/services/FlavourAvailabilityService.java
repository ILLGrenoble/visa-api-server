package eu.ill.visa.business.services;


import eu.ill.visa.cloud.domain.CloudResourceClass;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.core.domain.filters.InstanceFilter;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.core.entity.partial.DevicePoolUsage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Transactional
@Singleton
public class FlavourAvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(FlavourAvailabilityService.class);

    private final FlavourService flavourService;
    private final HypervisorService hypervisorService;
    private final CloudResourcesService cloudResourcesService;
    private final DevicePoolService devicePoolService;
    private final InstanceService instanceService;
    private final BookingTokenService bookingTokenService;

    @Inject
    public FlavourAvailabilityService(final FlavourService flavourService,
                                      final HypervisorService hypervisorService,
                                      final CloudResourcesService cloudResourcesService,
                                      final DevicePoolService devicePoolService,
                                      final InstanceService instanceService,
                                      final BookingTokenService bookingTokenService) {
        this.flavourService = flavourService;
        this.hypervisorService = hypervisorService;
        this.cloudResourcesService = cloudResourcesService;
        this.devicePoolService = devicePoolService;
        this.instanceService = instanceService;
        this.bookingTokenService = bookingTokenService;
    }

    public List<FlavourAvailability> getAllCurrentAvailabilities() {
        final List<Flavour> flavours = this.flavourService.getAllForAdmin();
        final Map<Long, SystemResources> allSystemResource = this.getAllAvailableSystemResources();

        return flavours.stream()
            .map(flavour -> {
                Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();

                return this.getAvailability(flavour, allSystemResource.get(cloudId));
            })
            .toList();
    }

    public FlavourAvailability getCurrentAvailability(final Flavour flavour) {
        final Map<Long, SystemResources> allSystemResource = this.getAllAvailableSystemResources();

        Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();

        return this.getAvailability(flavour, allSystemResource.get(cloudId));
    }

    public List<FlavourAvailability> getAllFirstAvailabilities() {
        final List<Flavour> flavours = this.flavourService.getAllForAdmin();
        final Map<Long, SystemResources> allSystemResource = this.getAllAvailableSystemResources();

        return flavours.stream()
            .map(flavour -> {
                Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();

                return this.getFirstAvailability(flavour, allSystemResource.get(cloudId));
            })
            .toList();
    }

    public List<FlavourAvailability> getAllFirstUnavailabilities() {
        final List<Flavour> flavours = this.flavourService.getAllForAdmin();
        final Map<Long, SystemResources> allSystemResource = this.getAllAvailableSystemResources();

        return flavours.stream()
            .map(flavour -> {
                Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();

                return this.getFirstUnavailability(flavour, allSystemResource.get(cloudId));
            })
            .toList();
    }

    public FlavourAvailability getFirstAvailability(final Flavour flavour) {
        Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();

        SystemResources systemResources = this.getAllAvailableSystemResources().get(cloudId);
        return this.getFirstAvailability(flavour, systemResources);
    }

    public FlavourAvailability getFirstUnavailability(final Flavour flavour) {
        Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();

        SystemResources systemResources = this.getAllAvailableSystemResources().get(cloudId);
        return this.getFirstUnavailability(flavour, systemResources);
    }

    public List<FlavourAvailability> getFutureAvailabilities(final Flavour flavour) {
        return this.getFutureAvailabilities(flavour, LocalDate.now(), LocalDate.now().plusYears(1000));
    }

    public List<FlavourAvailability> getFutureAvailabilities(final Flavour flavour, final LocalDate from, final LocalDate to) {
        final Map<Long, SystemResources> allSystemResource = this.getAllAvailableSystemResources();

        final Map<Long, List<ResourceUsageModifier>> cloudResourceUsageModifiers = this.getCloudResourceUsageModifiers();

        Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();

        return this.getFutureAvailabilities(flavour, allSystemResource.get(cloudId), cloudResourceUsageModifiers.get(cloudId), from, to);
    }

    public Map<Flavour, List<FlavourAvailability>> getAllFutureAvailabilities() {
        return this.getAllFutureAvailabilities(LocalDate.now(), LocalDate.now().plusYears(1000));
    }

    public Map<Flavour, List<FlavourAvailability>> getAllFutureAvailabilities(final LocalDate from, final LocalDate to) {
        final List<Flavour> flavours = this.flavourService.getAllForAdmin();
        return this.getFutureAvailabilities(flavours, from, to);
    }

    public Map<Flavour, List<FlavourAvailability>> getFutureAvailabilities(final List<Flavour> flavours) {
        return this.getFutureAvailabilities(flavours, LocalDate.now(), LocalDate.now().plusYears(1000));
    }

    public Map<Flavour, List<FlavourAvailability>> getFutureAvailabilities(final List<Flavour> flavours, final LocalDate from, final LocalDate to) {
        final Map<Long, SystemResources> allSystemResource = this.getAllAvailableSystemResources();
        final Map<Long, List<ResourceUsageModifier>> cloudResourceUsageModifiers = this.getCloudResourceUsageModifiers();

        return flavours.stream()
            .collect(Collectors.toMap(
                flavour -> flavour,
                flavour -> {
                    Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();

                    return this.getFutureAvailabilities(flavour, allSystemResource.get(cloudId), cloudResourceUsageModifiers.get(cloudId),  from, to);
                }
            ));
    }

    public Map<Flavour, List<FlavourAvailability>> calculateFutureAvailabilities(final List<Flavour> flavours, final BookingRequest bookingRequest) {
        final Map<Long, SystemResources> allSystemResource = this.getAllAvailableSystemResources();
        final Map<Long, List<ResourceUsageModifier>> cloudResourceUsageModifiers = this.getCloudResourceUsageModifiers(bookingRequest);

        final LocalDate from = bookingRequest.getStartDate().toLocalDate();
        final LocalDate to = bookingRequest.getEndDate().toLocalDate();

        return flavours.stream()
            .collect(Collectors.toMap(
                flavour -> flavour,
                flavour -> {
                    Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();

                    return this.getFutureAvailabilities(flavour, allSystemResource.get(cloudId), cloudResourceUsageModifiers.get(cloudId), from, to);
                }
            ));
    }

    private FlavourAvailability getAvailability(final Flavour flavour, final SystemResources systemResources) {
        if (systemResources == null) {
            // We don't know anything so return unknown response
            return new FlavourAvailability(new Date(), flavour, Optional.empty(), FlavourAvailability.AvailabilityConfidence.UNCERTAIN);

        } else {
            return systemResources.getAvailability(flavour);
        }
    }

    private FlavourAvailability getFirstAvailability(final Flavour flavour, final SystemResources systemResources) {
        // Check for immediate availability
        FlavourAvailability availability = this.getAvailability(flavour, systemResources);
        if (!availability.isAvailable().equals(FlavourAvailability.AvailabilityState.NO)) {
            return availability;
        }

        Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();
        SystemResources futureSystemResources = systemResources;

        final List<ResourceUsageModifier> resourceUsageModifiers = this.getCloudResourceUsageModifiers().get(cloudId);
        while (resourceUsageModifiers != null && !resourceUsageModifiers.isEmpty() && availability.hasUnits().equals(FlavourAvailability.AvailabilityState.NO)) {
            final ResourceUsageModifier resourceUsageModifier = resourceUsageModifiers.removeFirst();
            futureSystemResources = futureSystemResources.onResourcesModification(resourceUsageModifier);
            availability = this.getAvailability(flavour, futureSystemResources);
        }

        return availability;

    }

    private FlavourAvailability getFirstUnavailability(final Flavour flavour, final SystemResources systemResources) {
        // Check for immediate unavailability
        FlavourAvailability availability = this.getAvailability(flavour, systemResources);
        if (availability.isAvailable().equals(FlavourAvailability.AvailabilityState.NO)) {
            return availability;
        }

        Long cloudId = flavour.getCloudId() == null ? -1L : flavour.getCloudId();
        SystemResources futureSystemResources = systemResources;

        final List<ResourceUsageModifier> resourceUsageModifiers = this.getCloudResourceUsageModifiers().get(cloudId);
        while (resourceUsageModifiers != null && !resourceUsageModifiers.isEmpty() && !availability.hasUnits().equals(FlavourAvailability.AvailabilityState.NO)) {
            final ResourceUsageModifier resourceUsageModifier = resourceUsageModifiers.removeFirst();
            futureSystemResources = futureSystemResources.onResourcesModification(resourceUsageModifier);
            availability = this.getAvailability(flavour, futureSystemResources);
        }

        if (!availability.hasUnits().equals(FlavourAvailability.AvailabilityState.NO)) {
            return new FlavourAvailability(new Date(Long.MAX_VALUE), availability.flavour(), availability.availability(), availability.confidence());
        }
        return availability;
    }

    private List<FlavourAvailability> getFutureAvailabilities(final Flavour flavour, SystemResources systemResources, final List<ResourceUsageModifier> resourceUsageModifiers) {
        return this.getFutureAvailabilities(flavour, systemResources, resourceUsageModifiers, LocalDate.now(), LocalDate.now().plusYears(1000));
    }

    private List<FlavourAvailability> getFutureAvailabilities(final Flavour flavour, SystemResources systemResources, final List<ResourceUsageModifier> resourceUsageModifiers, final LocalDate from, final LocalDate to) {
        if (systemResources == null) {
            // We don't know anything so return unknown response
            return List.of(new FlavourAvailability(new Date(), flavour, Optional.empty(), FlavourAvailability.AvailabilityConfidence.UNCERTAIN));

        } else {
            List<FlavourAvailability> futures = new ArrayList<>();

            // Initialise array with the current availability
            LocalDate referenceDate = LocalDate.now();
            FlavourAvailability previousAvailability;
            FlavourAvailability currentAvailability = systemResources.getAvailability(flavour);
            if (!from.isAfter(referenceDate)) {
                futures.add(currentAvailability);
            }

            if (resourceUsageModifiers != null) {
                for (ResourceUsageModifier resourceUsageModifier : resourceUsageModifiers) {
                    systemResources = systemResources.onResourcesModification(resourceUsageModifier);
                    previousAvailability = currentAvailability;
                    currentAvailability = systemResources.getAvailability(flavour);

                    referenceDate = resourceUsageModifier.modificationDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    if (!from.isAfter(referenceDate) && !to.isBefore(referenceDate)) {
                        // Ensure that there is at least one value in the array covering the availability up until the 'from' moment
                        if (from.isBefore(referenceDate) && futures.isEmpty()) {
                            futures.add(previousAvailability);
                        }
                        futures.add(currentAvailability);
                    }
                }
            } else {
                // No resource usage modifiers, so put currentAvailability into the list
                futures.add(currentAvailability);
            }
            return futures;
        }
    }

    private Map<Long, SystemResources> getAllAvailableSystemResources() {

        Map<Long, SystemResources> allSystemResources = this.getAllSystemResources();

        // Remove all resources that are from active bookings but haven't got associated instances yet
        this.bookingTokenService.getAllActiveUnusedTokens().forEach(token -> {
            final ResourceUsageModifier modifier = ResourceUsageModifier.fromBookingTokenStart(token);
            Long cloudId = modifier.cloudId();
            SystemResources systemResources = allSystemResources.get(cloudId).onResourcesModification(modifier);
            allSystemResources.put(cloudId, systemResources);
        });

        return allSystemResources;
    }

    private Map<Long, SystemResources> getAllSystemResources() {
        final Map<Long, CloudResources> allCloudResources = this.getCloudResources();
        final Map<Long, List<Hypervisor>> cloudHypervisors = this.getCloudHypervisors();
        List<DevicePoolUsage> devicePoolUsages = devicePoolService.getDevicePoolUsage();

        Set<Long> allCloudClientIds = new HashSet<>(allCloudResources.keySet());
        allCloudClientIds.addAll(cloudHypervisors.keySet());

        return allCloudClientIds.stream()
            .collect(Collectors.toMap(
                cloudClientId -> cloudClientId,
                cloudClientId -> {
                    final CloudResources cloudResources = allCloudResources.get(cloudClientId);
                    final List<Hypervisor> hypervisors = cloudHypervisors.get(cloudClientId);
                    final List<HypervisorInventory> hypervisorInventories = hypervisors == null ? new ArrayList<>() : hypervisors.stream()
                        .map(hypervisor -> {
                            long cpusAvailable = hypervisor.getAvailableResource(CloudResourceClass.VCPU_RESOURCE_CLASS);
                            long ramMBAvailable = hypervisor.getAvailableResource(CloudResourceClass.MEMORY_MB_RESOURCE_CLASS);
                            long cpusTotal = hypervisor.getTotalResource(CloudResourceClass.VCPU_RESOURCE_CLASS);
                            long ramMBTotal = hypervisor.getTotalResource(CloudResourceClass.MEMORY_MB_RESOURCE_CLASS);
                            List<String> serverComputeIds = hypervisor.getAllocations().stream().map(HypervisorAllocation::getServerComputeId).toList();
                            return new HypervisorInventory(hypervisor.getId(), hypervisor.getHostname(), cpusAvailable, cpusTotal, ramMBAvailable, ramMBTotal, hypervisor.getResources(), serverComputeIds);
                        })
                        .toList();

                    return new SystemResources(cloudClientId, cloudResources, devicePoolUsages, hypervisorInventories);
                }
            ));
    }

    private Map<Long, CloudResources> getCloudResources() {
        return this.cloudResourcesService.getAll().stream()
            .collect(Collectors.toMap(
                cloudResources -> cloudResources.getCloudId() == null ? - 1 : cloudResources.getCloudId(),
                cloudResources -> cloudResources
            ));
    }

    private Map<Long, List<Hypervisor>> getCloudHypervisors() {
        final List<Hypervisor> hypervisors = this.hypervisorService.getAllAvailable();
        return hypervisors.stream()
            .collect(Collectors.groupingBy(hypervisor -> hypervisor.getCloudId() == null ? -1 : hypervisor.getCloudId()));
    }

    private Map<Long, List<ResourceUsageModifier>> getCloudResourceUsageModifiers() {
        return this.getCloudResourceUsageModifiers(null);
    }

    private Map<Long, List<ResourceUsageModifier>> getCloudResourceUsageModifiers(final BookingRequest bookingRequest) {
        final List<ResourceUsageModifier> allModifiers = this.getInstanceTerminationResourceUsageModifiers();
        allModifiers.addAll(this.getFutureBookingResourceUsageModifiers(bookingRequest));
//        allModifiers.sort((m1, m2) -> m1.modificationDate().before(m2.modificationDate()) ? -1 : m1.modificationDate().equals(m2.modificationDate()) ? 0 : 1);
        Map<Long, List<ResourceUsageModifier>> resourceUsageModifiers = allModifiers.stream().collect(Collectors.groupingBy(ResourceUsageModifier::cloudId));

        // Combine multiple modifiers with the same date into a single modifier
        return resourceUsageModifiers.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> {
                    List<ResourceUsageModifier> reduced = this.reduceResourceUsageModifiers(e.getValue());
                    reduced.sort((m1, m2) -> m1.modificationDate().before(m2.modificationDate()) ? -1 : m1.modificationDate().equals(m2.modificationDate()) ? 0 : 1);
                    return reduced;
                }
            ));
    }

    private List<ResourceUsageModifier> reduceResourceUsageModifiers(final List<ResourceUsageModifier> resourceUsageModifiers) {
        return new ArrayList<>(resourceUsageModifiers.stream()
            .collect(Collectors.groupingBy(
                ResourceUsageModifier::modificationDate,
                Collectors.reducing(
                    null,
                    m -> m,
                    (a, b) -> b.combine(a)
                )
            ))
            .values());
    }

    private List<ResourceUsageModifier> getInstanceTerminationResourceUsageModifiers() {
        return this.instanceService.getAll(new InstanceFilter(), new OrderBy("terminationDate", true)).stream()
            .filter(instance -> instance.getTerminationDate() != null)
            .map(ResourceUsageModifier::fromInstanceTermination)
            .collect(Collectors.toList());
    }

    private List<ResourceUsageModifier> getFutureBookingResourceUsageModifiers(final BookingRequest bookingRequest) {
        List<ResourceUsageModifier> futureModifiers = this.bookingTokenService.getAllFutureTokens().stream()
            .flatMap(token -> {
                    ResourceUsageModifier startModifier = ResourceUsageModifier.fromBookingTokenStart(token);
                    ResourceUsageModifier endModifier = ResourceUsageModifier.fromBookingTokenEnd(token);
                    return Stream.of(startModifier, endModifier);
                }
            ).collect(Collectors.toList());

        List <ResourceUsageModifier> currentUnusedModifiers = this.bookingTokenService.getAllActiveUnusedTokens().stream()
            .map(ResourceUsageModifier::fromBookingTokenEnd)
            .toList();
        futureModifiers.addAll(currentUnusedModifiers);

        // If a booking request has been passed then create resource usages from the expected tokens too
        if (bookingRequest != null) {
            List<ResourceUsageModifier> bookingRequestUsageModifiers = bookingRequest.getFlavours().stream()
                .flatMap(requestFlavour -> {
                    List<BookingToken> bookingTokensForFlavour = new ArrayList<>();
                    for (long i = 0; i < requestFlavour.getQuantity(); i++) {
                        bookingTokensForFlavour.add(new BookingToken(bookingRequest, requestFlavour.getFlavour(), ""));
                    }
                    return bookingTokensForFlavour.stream();
                })
                .flatMap(token -> {
                        ResourceUsageModifier startModifier = ResourceUsageModifier.fromBookingTokenStart(token);
                        ResourceUsageModifier endModifier = ResourceUsageModifier.fromBookingTokenEnd(token);
                        return Stream.of(startModifier, endModifier);
                    }
                ).toList();
            futureModifiers.addAll(bookingRequestUsageModifiers);
        }

        return futureModifiers;
    }

}
