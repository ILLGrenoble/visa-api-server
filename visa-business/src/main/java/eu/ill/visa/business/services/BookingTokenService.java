package eu.ill.visa.business.services;

import eu.ill.visa.business.notification.EmailManager;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.persistence.repositories.BookingTokenRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Transactional
@Singleton
public class BookingTokenService {
    private static final Logger logger = LoggerFactory.getLogger(BookingTokenService.class);

    private final BookingTokenRepository repository;
    private final EmailManager emailManager;

    @Inject
    public BookingTokenService(final BookingTokenRepository repository,
                               final EmailManager emailManager) {
        this.repository = repository;
        this.emailManager = emailManager;
    }

    public BookingToken getById(Long id) {
        return this.repository.getById(id);
    }

    public BookingToken getByUid(String uid) {
        return this.repository.getByUid(uid);
    }

    public List<BookingToken> getAllForBookingRequest(final BookingRequest bookingRequest) {
        // Ensure that we have all valid tokens (in case they weren't created initially or request has been updated)
        return this.createBookingTokensForBookingRequest(bookingRequest);
    }

    public List<BookingToken> getAllAssignedToUser(final User user) {
        return this.repository.getAllAssignedToUserId(user.getId());
    }

    /**
     * Returns all BookingTokens for active BookingRequests that haven't been used yet
     */
    public List<BookingToken> getAllActiveUnusedTokens() {
        return this.repository.getAllActiveUnusedTokens();
    }

    public List<BookingToken> getAllFutureTokens() {
        return this.repository.getAllFutureTokens();
    }

    public void save(final BookingToken bookingToken) {
        this.repository.save(bookingToken);
    }

    public void updateTokenOwners(final BookingRequest bookingRequest, final Map<Long, User> tokenIdOwners) {
        final List<BookingToken> allTokens = this.getAllForBookingRequest(bookingRequest);

        final Set<User> originalOwners = allTokens.stream()
            .map(BookingToken::getOwner)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        final Set<User> newOwners = new HashSet<>();
        final List<BookingToken> updatedTokens = new ArrayList<>();

        for (final Map.Entry<Long, User> entry : tokenIdOwners.entrySet()) {
            final Long tokenId = entry.getKey();
            final User owner = entry.getValue();

            final BookingToken token = allTokens.stream().filter(aToken -> aToken.getId().equals(tokenId)).findFirst().orElse(null);
            if (token != null) {
                if ((token.getOwner() != null && !token.getOwner().equals(owner)) || (owner != null && !owner.equals(token.getOwner()))) {
                    token.setOwner(owner);
                    updatedTokens.add(token);
                    if (owner != null && !originalOwners.contains(owner)) {
                        newOwners.add(owner);
                    }
                }
            }
        }

        for (BookingToken bookingToken : updatedTokens) {
            this.save(bookingToken);
        }

        // Email all new booking token owners
        for (User owner : newOwners) {
            this.emailManager.sendBookingRequestTokenNotification(bookingRequest, owner);
        }
    }

    public void delete(@NotNull BookingToken bookingToken) {
        bookingToken.setDeletedAt(new Date());
        this.save(bookingToken);
    }

    public List<BookingToken> createBookingTokensForBookingRequest(BookingRequest bookingRequest) {
        List<BookingToken> bookingTokens = this.repository.getAllForBookingRequestId(bookingRequest.getId());

        for (BookingRequestFlavour requestFlavour : bookingRequest.getFlavours()) {
            List<BookingToken> bookingTokensForFlavour = bookingTokens.stream().filter(bookingToken -> bookingToken.getFlavour().equals(requestFlavour.getFlavour())).collect(Collectors.toList());

            while (bookingTokensForFlavour.size() < requestFlavour.getQuantity()) {
                BookingToken bookingToken = new BookingToken(bookingRequest, requestFlavour.getFlavour(), this.getUID());
                this.save(bookingToken);
                bookingTokensForFlavour.add(bookingToken);
            }
        }

        return this.repository.getAllForBookingRequestId(bookingRequest.getId());
    }

    public String getUID() {
        String regex = "^.*[a-zA-Z]+.*";
        Pattern pattern = Pattern.compile(regex);

        do {
            String uid = RandomStringUtils.randomAlphanumeric(8);

            // Ensure UID has at least one character to make it distinguishable from a valid ID
            Matcher matcher = pattern.matcher(uid);

            if (matcher.matches() && this.repository.getByUid(uid) == null) {
                return uid;
            }
        } while (true);
    }

    public void updateBookingToken(BookingToken bookingToken, Instance instance, User user) {
        bookingToken.setInstance(instance);
        bookingToken.getHistory().add(new BookingTokenHistory(user, instance));
        this.save(bookingToken);
    }

    public BookingToken getForInstanceId(Long id) {
        return this.repository.getForInstanceId(id);
    }
}
