/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2015 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2015. Contribute to
 * Linshare R&D by subscribing to an Enterprise offer!” infobox and in the
 * e-mails sent with the Program, (ii) retain all hypertext links between
 * LinShare and linshare.org, between linagora.com and Linagora, and (iii)
 * refrain from infringing Linagora intellectual property rights over its
 * trademarks and commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for LinShare along with this program. If not,
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to LinShare software.
 */
package org.linagora.linshare.core.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.linagora.linshare.core.business.service.DomainPermissionBusinessService;
import org.linagora.linshare.core.domain.constants.AccountType;
import org.linagora.linshare.core.domain.constants.Language;
import org.linagora.linshare.core.domain.constants.LogAction;
import org.linagora.linshare.core.domain.constants.Role;
import org.linagora.linshare.core.domain.constants.SupportedLanguage;
import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.AllowedContact;
import org.linagora.linshare.core.domain.entities.Functionality;
import org.linagora.linshare.core.domain.entities.Guest;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.domain.entities.UserLogEntry;
import org.linagora.linshare.core.domain.vo.UserVo;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.exception.TechnicalErrorCode;
import org.linagora.linshare.core.exception.TechnicalException;
import org.linagora.linshare.core.repository.AllowedContactRepository;
import org.linagora.linshare.core.repository.GuestRepository;
import org.linagora.linshare.core.repository.UserRepository;
import org.linagora.linshare.core.service.AbstractDomainService;
import org.linagora.linshare.core.service.EntryService;
import org.linagora.linshare.core.service.FunctionalityReadOnlyService;
import org.linagora.linshare.core.service.LogEntryService;
import org.linagora.linshare.core.service.ThreadService;
import org.linagora.linshare.core.service.UserService;
import org.linagora.linshare.core.utils.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

/**
 * Services for User management.
 */
public class UserServiceImpl implements UserService {

	final private static Logger logger = LoggerFactory
			.getLogger(UserServiceImpl.class);

	private final UserRepository<User> userRepository;

	private final LogEntryService logEntryService;

	private final AbstractDomainService abstractDomainService;

	private final FunctionalityReadOnlyService functionalityReadOnlyService;

	private final EntryService entryService;

	/** User repository. */
	private final GuestRepository guestRepository;

	private final AllowedContactRepository allowedContactRepository;

	private final ThreadService threadService;

	private final DomainPermissionBusinessService domainPermisionService;

	public UserServiceImpl(
			final UserRepository<User> userRepository,
			final LogEntryService logEntryService,
			final GuestRepository guestRepository,
			final AllowedContactRepository allowedContactRepository,
			final FunctionalityReadOnlyService functionalityService,
			final AbstractDomainService abstractDomainService,
			final EntryService entryService,
			final ThreadService threadService,
			final DomainPermissionBusinessService domainPermissionBusinessService) {

		this.userRepository = userRepository;
		this.logEntryService = logEntryService;
		this.guestRepository = guestRepository;
		this.allowedContactRepository = allowedContactRepository;
		this.abstractDomainService = abstractDomainService;
		this.functionalityReadOnlyService = functionalityService;
		this.entryService = entryService;
		this.threadService = threadService;
		this.domainPermisionService = domainPermissionBusinessService;
	}

	@Override
	public User findUserInDB(String domain, String mail) {
		return userRepository.findByMailAndDomain(domain, mail);
	}

	@Deprecated
	@Override
	public User findUnkownUserInDB(String mail) {
		return userRepository.findByMail(mail);
	}

	@Override
	public User findByLsUuid(String lsUuid) {
		Validate.notEmpty(lsUuid, "User uuid must be set.");
		User user = userRepository.findByLsUuid(lsUuid);
		if (user == null)
			throw new BusinessException(BusinessErrorCode.USER_NOT_FOUND, "User with uuid : " + lsUuid  + " not found.");
		return user;
	}

	@Override
	public boolean exist(String lsUuid) {
		return userRepository.findByLsUuid(lsUuid) != null;
	}

	@Override
	public List<User> findUsersInDB(String domain) {
		return userRepository.findByDomain(domain);
	}

	@Override
	public void deleteUser(Account actor, String lsUuid)
			throws BusinessException {
		User user = userRepository.findByLsUuid(lsUuid);

		if (user != null) {
			boolean hasRightToDeleteThisUser = isAdminForThisUser(actor, user);

			logger.debug("Has right ? : " + hasRightToDeleteThisUser);

			if (!hasRightToDeleteThisUser) {
				throw new BusinessException(
						BusinessErrorCode.CANNOT_DELETE_USER, "The user "
								+ lsUuid
								+ " cannot be deleted, he is not a guest, or "
								+ actor.getAccountReprentation()
								+ " is not an admin");
			} else {
				setUserToDestroy(actor, user);
			}
		} else {
			logger.debug("User not found in DB : " + lsUuid);
		}
	}

	@Override
	public void deleteAllUsersFromDomain(User actor, String domainIdentifier)
			throws BusinessException {
		logger.debug("deleteAllUsersFromDomain: begin");

		List<User> users = userRepository.findByDomain(domainIdentifier);

		logger.info("Delete all user from domain " + domainIdentifier
				+ ", count: " + users.size());

		for (User user : users) {
			setUserToDestroy(actor, user);
		}
		logger.debug("deleteAllUsersFromDomain: end");
	}

	@Override
	public boolean isAdminForThisUser(Account actor, User user) {
		return domainPermisionService.isAdminForThisUser(actor, user);
	}

	private void setUserToDestroy(Account actor, User userToDelete)
			throws BusinessException {
		try {
			// clear all thread memberships
			threadService.deleteAllUserMemberships(actor, userToDelete);

			userRepository.delete(userToDelete);

			UserLogEntry logEntry = new UserLogEntry(actor,
					LogAction.USER_DELETE, "Deleting an user", userToDelete);
			logEntryService.create(logEntry);

		} catch (IllegalArgumentException e) {
			logger.error(
					"Couldn't find the user "
							+ userToDelete.getAccountReprentation()
							+ " to be deleted", e);
			throw new BusinessException(BusinessErrorCode.USER_NOT_FOUND,
					"Couldn't find the user "
							+ userToDelete.getAccountReprentation()
							+ " to be deleted");
		}

	}


	/**
	 * Legacy code, saved for future batch delete
	 */

	@Override
	public void markToPurge(Account actor, String lsUuid) throws BusinessException {
		User user = userRepository.findDeleted(lsUuid);
		boolean hasRightToDeleteThisUser = isAdminForThisUser(actor, user);

		logger.debug("Has right ? : " + hasRightToDeleteThisUser);

		if (!hasRightToDeleteThisUser) {
			throw new BusinessException(
					BusinessErrorCode.CANNOT_DELETE_USER, "The user "
							+ lsUuid
							+ " cannot be deleted, he is not a guest, or "
							+ actor.getAccountReprentation()
							+ " is not an admin");
		} else {
			userRepository.markToPurge(user);
		}
	}

	@Override
	public void purge(Account actor, String lsUuid)
			throws BusinessException {

		User userToDelete = userRepository.findDeleted(lsUuid);

		try {
			entryService.deleteAllReceivedShareEntries(actor, userToDelete);
			entryService.deleteAllShareEntriesWithDocumentEntries(actor,
					userToDelete);

			// clearing the favorites
			// recipientFavouriteService.deleteFavoritesOfUser(userToDelete);

			// clearing allowed contacts
			allowedContactRepository.deleteAllByUserBothSides(userToDelete);

			// clear all thread memberships
			threadService.deleteAllUserMemberships(actor, userToDelete);

			// // clearing all signatures
			// Set<Signature> ownSignatures = userToDelete.getOwnSignatures();
			// ownSignatures.clear();
			// userRepository.update(userToDelete);

			userRepository.purge(userToDelete);

			UserLogEntry logEntry = new UserLogEntry(actor,
					LogAction.USER_DELETE, "Deleting an user", userToDelete);
			logEntryService.create(logEntry);

		} catch (IllegalArgumentException e) {
			logger.error(
					"Couldn't find the user "
							+ userToDelete.getAccountReprentation()
							+ " to be deleted", e);
			throw new BusinessException(BusinessErrorCode.USER_NOT_FOUND,
					"Couldn't find the user "
							+ userToDelete.getAccountReprentation()
							+ " to be deleted");
		}
	}

	private List<User> searchForRestrictedGuest(Guest owner, String mail, String firstname, String lastname) {
		// TODO : FIXME : OPTIMISATION NEEDED : This method should return a User
		// list instead of AllowedContact.
		List<AllowedContact> contacts = allowedContactRepository.searchContact(owner,
				mail, firstname, lastname);
		return toUsers(contacts);
	}

	private List<User> completionSearchForRestrictedGuest(Guest actor, String firstname, String lastname) {
		// TODO : FIXME : OPTIMISATION NEEDED : This method should return a User
		// list instead of AllowedContact.
		List<AllowedContact> contacts = allowedContactRepository.completeContact(actor,
				firstname, lastname);
		return toUsers(contacts);
	}

	private List<User> completionSearchForRestrictedGuest(Guest actor,
			String pattern) {
		// TODO : FIXME : OPTIMISATION NEEDED : This method should return a User
		// list instead of AllowedContact.
		List<AllowedContact> contacts = allowedContactRepository.completeContact(actor, pattern);
		return toUsers(contacts);
	}

	private List<User> toUsers(List<AllowedContact> contacts) {
		List<User> users = Lists.newArrayList();
		for (AllowedContact allowedContact : contacts) {
			users.add(allowedContact.getContact());
		}
		return users;
	}

	/**
	 * TODO : FIXME : OPTIMISATION NEEDED : This method should be refactor to
	 * search guests domain by domain
	 * 
	 * @param actor
	 *            : the current user performing this research.
	 * @param mail
	 *            : mail pattern. Not used if null.
	 * @param firstName
	 *            : first name pattern. Not used if null.
	 * @param lastName
	 *            : last name pattern. Not used if null.
	 * @return
	 * @throws BusinessException 
	 */
	private List<User> searchOnGuest(Account actor, String mail,
			String firstName, String lastName) throws BusinessException {
		List<User> result = new ArrayList<User>();
		logger.debug("adding guests to the return list");

		// TODO : FIXME : OPTIMISATION NEEDED : This method should be refactored
		// to search guests domain by domain
		List<Guest> list = guestRepository.searchGuestAnyWhere(mail, firstName,
				lastName);
		logger.debug("Guest found : size : " + list.size());

		List<AbstractDomain> allAuthorizedDomain = abstractDomainService
				.getAllAuthorizedDomains(actor.getDomain().getIdentifier());
		List<String> allAuthorizedDomainIdentifier = new ArrayList<String>();

		for (AbstractDomain d : allAuthorizedDomain) {
			allAuthorizedDomainIdentifier.add(d.getIdentifier());
		}

		for (Guest guest : list) {
			if (allAuthorizedDomainIdentifier.contains(guest.getDomainId())) {
				result.add(guest);
			}
		}

		logger.debug("result guest list : size : " + result.size());
		return result;
	}

	/**
	 * TODO : FIXME : OPTIMISATION NEEDED : This method should be refactor to
	 * search guests domain by domain
	 *
	 * @param actor
	 *            : the current user performing this research.
	 * @param mail
	 *            : mail pattern. Not used if null.
	 * @param firstName
	 *            : first name pattern. Not used if null.
	 * @param lastName
	 *            : last name pattern. Not used if null.
	 * @return
	 * @throws BusinessException 
	 */
	private List<User> completionSearchOnGuest(Account actor, String pattern) throws BusinessException {
		List<User> result = new ArrayList<User>();
		logger.debug("adding guests to the return list");

		// TODO : FIXME : OPTIMISATION NEEDED : This method should be refactored
		// to search guests domain by domain
		List<Guest> list = guestRepository.searchGuestAnyWhere(pattern);
		logger.debug("Guest found : size : " + list.size());

		List<AbstractDomain> allAuthorizedDomain = abstractDomainService
				.getAllAuthorizedDomains(actor.getDomain().getIdentifier());
		List<String> allAuthorizedDomainIdentifier = new ArrayList<String>();

		for (AbstractDomain d : allAuthorizedDomain) {
			allAuthorizedDomainIdentifier.add(d.getIdentifier());
		}

		for (Guest guest : list) {
			if (allAuthorizedDomainIdentifier.contains(guest.getDomainId())) {
				result.add(guest);
			}
		}

		logger.debug("result guest list : size : " + result.size());
		return result;
	}

	private List<User> completionSearchOnGuest(Account actor, String firstName, String lastName) throws BusinessException {
		List<User> result = new ArrayList<User>();
		logger.debug("adding guests to the return list");

		// TODO : FIXME : OPTIMISATION NEEDED : This method should be refactored
		// to search guests domain by domain
		List<Guest> list = guestRepository.searchGuestAnyWhere(firstName, lastName);
		logger.debug("Guest found : size : " + list.size());

		List<AbstractDomain> allAuthorizedDomain = abstractDomainService
				.getAllAuthorizedDomains(actor.getDomain().getIdentifier());
		List<String> allAuthorizedDomainIdentifier = new ArrayList<String>();

		for (AbstractDomain d : allAuthorizedDomain) {
			allAuthorizedDomainIdentifier.add(d.getIdentifier());
		}

		for (Guest guest : list) {
			if (allAuthorizedDomainIdentifier.contains(guest.getDomainId())) {
				result.add(guest);
			}
		}

		logger.debug("result guest list : size : " + result.size());
		return result;
	}

	/**
	 * @param actor
	 *            : the current user performing this research.
	 * @param pattern
	 *            : mail pattern. Not used if null.
	 * @param firstName
	 *            : first name pattern. Not used if null.
	 * @param lastName
	 *            : last name pattern. Not used if null.
	 * @return
	 */
	private List<User> completionSearchOnInternal(User actor, String pattern)
			throws BusinessException {
		List<User> internals = abstractDomainService
				.autoCompleteUserWithDomainPolicies(actor.getDomain()
						.getIdentifier(), pattern);
		logger.debug("result internals list : size : " + internals.size());
		return internals;
	}

	private List<User> completionSearchOnInternal(User actor, String firstName,
			String lastName) throws BusinessException {
		List<User> internals = abstractDomainService
				.autoCompleteUserWithDomainPolicies(actor.getDomain()
						.getIdentifier(), firstName, lastName);
		logger.debug("result internals list : size : " + internals.size());
		return internals;
	}

	private User findUser(String lsUuid) throws BusinessException {
		lsUuid = StringUtils.trim(lsUuid);
		User user = userRepository.findByLsUuid(lsUuid);
		if (user == null) {
			String errMsg = "Can not find current actor : " + lsUuid;
			logger.error(errMsg);
			throw new BusinessException(BusinessErrorCode.USER_NOT_FOUND,
					errMsg);
		}
		return user;
	}

	@Override
	public List<User> autoCompleteUser(Account actor, String pattern)
			throws BusinessException {
		logger.debug("Begin autoCompleteUser");

		// Checking input parameters
		pattern = StringUtils.trim(pattern);

		List<User> users = new ArrayList<User>();

		// building search parameters.
		String mail = pattern;
		String firstName = null;
		String lastName = null;

		// Splitting pattern into firstName and lastName
		// First argument is the first name, the second if exists is last name
		StringTokenizer stringTokenizer = new StringTokenizer(pattern, " ");
		if (stringTokenizer.hasMoreTokens()) {
			firstName = stringTokenizer.nextToken();
			if (stringTokenizer.hasMoreTokens()) {
				lastName = stringTokenizer.nextToken();
			}
		}

		// TODO : Only guest could be restricted ? why some internal could not
		// be ?
		if (actor.isGuest()) {
			// RESTRICTED GUEST MUST NOT SEE ALL USERS
			if (((Guest)actor).isRestricted()) {
				if (lastName == null) {
					return completionSearchForRestrictedGuest((Guest)actor, mail);
				} else {
					return completionSearchForRestrictedGuest((Guest)actor, firstName, lastName);
				}
			}
		}

		// completion on database for guests
		if (lastName == null) {
			users.addAll(completionSearchOnGuest(actor, mail));
		} else {
			users.addAll(completionSearchOnGuest(actor, firstName, lastName));
		}

		// completion on LDAP directory for internals
		if (lastName == null) {
			logger.debug("completionSearchOnInternal: mail");
			users.addAll(completionSearchOnInternal((User)actor, mail));
		} else {
			logger.debug("completionSearchOnInternal: first name and last name");
			users.addAll(completionSearchOnInternal((User)actor, firstName,
					lastName));
		}

		logger.debug("End autoCompleteUser");
		return users;

	}

	@Override
	public List<User> searchUser(String mail, String firstName,
			String lastName, AccountType userType, User currentUser)
			throws BusinessException {

		logger.debug("Begin searchUser");
		List<User> users = new ArrayList<User>();

		if (currentUser != null
				&& currentUser.getAccountType() == AccountType.GUEST) { // GUEST
																		// RESTRICTED
																		// MUST
																		// NOT
																		// SEE
																		// ALL
																		// USERS
			Guest currentGuest = guestRepository.findByLsUuid(currentUser.getLsUuid());
			if (currentGuest.isRestricted()) {
				return searchForRestrictedGuest(currentGuest, mail,
						firstName, lastName);
			}
		}

		if (null == userType || userType.equals(AccountType.GUEST)) {
			users.addAll(searchOnGuest(currentUser, mail, firstName, lastName));
		}
		if (null == userType || userType.equals(AccountType.INTERNAL)) {
			List<User> internals = abstractDomainService
					.searchUserWithDomainPolicies(currentUser.getDomain()
							.getIdentifier(), mail, firstName, lastName);
			logger.debug("result internals list : size : " + internals.size());
			users.addAll(internals);
		}

		logger.debug("End searchUser");
		return users;
	}

	@Override
	public void updateUserRole(String userUuid, String domain, String mail,
			Role role, UserVo ownerVo) throws BusinessException {
		User user = userRepository.findByLsUuid(userUuid);
		if (user == null) {
			logger.debug("User "
					+ mail
					+ " was not found in the database. Searching in directories ...");
			user = searchAndCreateUserEntityFromDirectory(domain, mail);
		}
		if (user == null) {
			throw new TechnicalException(TechnicalErrorCode.USER_INCOHERENCE,
					"Couldn't find the user : " + mail + " in domain : "
							+ domain);
		} else {
			logger.debug("User " + mail + " found.");
			user.setRole(role);
			if (!(user.getRole().equals(Role.SIMPLE) || user.getRole().equals(Role.ADMIN))) {
				user.setRole(Role.SIMPLE);
			}
			userRepository.update(user);
			User owner = userRepository.findByLsUuid(ownerVo.getLsUuid());
			UserLogEntry logEntry = new UserLogEntry(owner,
					LogAction.USER_UPDATE,
					"Update of a user:" + user.getMail(), user);
			logEntryService.create(logEntry);
		}
	}

	@Override
	public void updateUserLocale(String domainId, String mail, SupportedLanguage locale)
			throws BusinessException {
		User user = findOrCreateUser(mail, domainId);
		if (user == null) {
			throw new TechnicalException(TechnicalErrorCode.USER_INCOHERENCE,
					"Couldn't find the user " + mail);
		}
		user.setLocale(locale);
		try {
			user = userRepository.update(user);
		} catch (IllegalArgumentException e) {
			throw new TechnicalException(TechnicalErrorCode.USER_INCOHERENCE,
					"Couldn't find the user " + mail);
		} catch (BusinessException e) {
			throw new TechnicalException(TechnicalErrorCode.USER_INCOHERENCE,
					"Couldn't save the locale " + locale);
		}

	}

	@Override
	public void updateUserExternalMailLocale(String domainId, String mail,
			Language externalMailLocale) throws BusinessException {
		User user = findOrCreateUser(mail, domainId);
		if (user == null) {
			throw new TechnicalException(TechnicalErrorCode.USER_INCOHERENCE,
					"Couldn't find the user " + mail);
		}
		user.setExternalMailLocale(externalMailLocale);
		try {
			user = userRepository.update(user);
		} catch (IllegalArgumentException e) {
			throw new TechnicalException(TechnicalErrorCode.USER_INCOHERENCE,
					"Couldn't find the user " + mail);
		} catch (BusinessException e) {
			throw new TechnicalException(TechnicalErrorCode.USER_INCOHERENCE,
					"Couldn't save the locale " + externalMailLocale);
		}
	}

	@Override
	public void updateUserLocale(String domainId, String mail, SupportedLanguage locale,
			Language externalMailLocale, String cmisLocale) throws BusinessException {
		User user = findOrCreateUser(mail, domainId);
		if (user == null) {
			throw new TechnicalException(TechnicalErrorCode.USER_INCOHERENCE,
					"Couldn't find the user " + mail);
		}
		user.setLocale(locale);
		user.setExternalMailLocale(externalMailLocale);
		user.setCmisLocale(cmisLocale);
		userRepository.update(user);
	}

	@Override
	public void changePassword(String uuid, String mail, String oldPassword,
			String newPassword) throws BusinessException {
		User user = userRepository.findByLsUuid(uuid);
		if (user == null) {
			throw new TechnicalException(TechnicalErrorCode.USER_INCOHERENCE,
					"Could not find a user with the login " + mail);
		}

		if (!user.getPassword().equals(
				HashUtils.hashSha1withBase64(oldPassword.getBytes()))) {
			throw new BusinessException(BusinessErrorCode.AUTHENTICATION_ERROR,
					"The supplied password is invalid");
		}

		user.setPassword(HashUtils.hashSha1withBase64(newPassword.getBytes()));
		userRepository.update(user);
	}

	@Override
	public void updateUserDomain(String mail, String selectedDomain,
			UserVo ownerVo) throws BusinessException {
		if (!ownerVo.isSuperAdmin()) {
			throw new BusinessException(BusinessErrorCode.CANNOT_UPDATE_USER,
					"The user " + mail + " cannot be moved to "
							+ selectedDomain + " domain, " + ownerVo.getMail()
							+ " is not a superadmin");
		}
		User user = null;
		// Seek user in base. If not found, try again but in directories
		if ((user = userRepository.findByMail(mail)) == null) {
			try {
				user = findOrCreateUser(mail, ownerVo.getDomainIdentifier());
			} catch (BusinessException e) {
				logger.error(e.toString());
				throw e;
			}
		}
		AbstractDomain newDomain = abstractDomainService
				.retrieveDomain(selectedDomain);
		user.setDomain(newDomain);
		userRepository.update(user);
	}

	@Override
	public List<User> searchAllBreakedUsers(User actor) {
		List<User> users = userRepository.findAll();
		List<User> internalsBreaked = new ArrayList<User>();

		logger.debug("System is about to process internal user existence test : "
				+ users.size());
		for (User user : users) {
			if (user.getAccountType().equals(AccountType.INTERNAL)) {
				if (!(user.getRole().equals(Role.SYSTEM) || user.getRole()
						.equals(Role.SUPERADMIN))) { // hide
														// these
														// accounts
					try {
						if (!abstractDomainService.isUserExist(
								user.getDomain(), user.getMail())) {
							internalsBreaked.add(user);
						}
					} catch (BusinessException e) {
						logger.error(
								"Error while searching inconsistent users", e);
					}
				}
			}
		}
		return internalsBreaked;
	}

	@Override
	public User saveOrUpdateUser(User user) throws TechnicalException {
		// User object should be an new entity, or an existing one
		logger.debug("Begin saveOrUpdateUser");
		if (user != null && user.getDomain() != null) {
			logger.debug("Trying to find the current user in the user repository.");
			logger.debug("mail:" + user.getMail());
			logger.debug("domain id:" + user.getDomainId());
			User existingUser = userRepository.findByMailAndDomain(user
					.getDomain().getIdentifier(), user.getMail());
			if (existingUser != null) {
				// update
				logger.debug("userRepository.update(existingUser)");
				try {
					return userRepository.update(existingUser);
				} catch (IllegalArgumentException e) {
					logger.error("Could not update the user " + user.getMail()
							+ " in the database ", e);
					throw new TechnicalException(
							TechnicalErrorCode.USER_INCOHERENCE,
							"The user could not be update in the DB " + e);
				} catch (BusinessException e) {
					logger.error("Could not update the user " + user.getMail()
							+ " in the database ", e);
					throw new TechnicalException(
							TechnicalErrorCode.USER_INCOHERENCE,
							"The user could not be update in the DB " + e);
				}
			} else {
				logger.debug("userRepository.create(user)");
				// create
				Functionality guestfunc = functionalityReadOnlyService
						.getGuests(user.getDomain());
				user.setCanCreateGuest(guestfunc.getActivationPolicy()
						.getStatus());
				Functionality userCanUploadFunc = functionalityReadOnlyService
						.getUserCanUploadFunctionality(user.getDomain());
				user.setCanUpload(userCanUploadFunc.getActivationPolicy()
						.getStatus());
				user.setCreationDate(new Date());
				user.setLocale(user.getDomain().getDefaultTapestryLocale());
				// TODO : FIXME : Waiting to get default external mail local from domain.
				Language locale = Language.fromTapestryLocale(user.getLocale().getTapestryLocale());
				user.setExternalMailLocale(locale);
				user.setCmisLocale(user.getDomain().getDefaultTapestryLocale().toString());
				try {
					return userRepository.create(user);
				} catch (IllegalArgumentException e) {
					logger.error("Could not create the user " + user.getMail()
							+ " in the database ", e);
					throw new TechnicalException(
							TechnicalErrorCode.USER_INCOHERENCE,
							"The user could not be created in the DB " + e);
				} catch (BusinessException e) {
					logger.error("Could not create the user " + user.getMail()
							+ " in the database ", e);
					throw new TechnicalException(
							TechnicalErrorCode.USER_INCOHERENCE,
							"The user could not be created in the DB " + e);
				}
			}
		} else {
			String msg;
			if (user != null) {
				msg = "Attempt to create or update an user entity failed : User domain object is null.";
			} else {
				msg = "Attempt to create or update an user entity failed : User object is null.";
			}

			logger.debug(msg);
			logger.debug("End saveOrUpdateUser");
			throw new TechnicalException(TechnicalErrorCode.USER_INCOHERENCE,
					msg);
		}
	}

	/**
	 * This method search a user on 1 domain without domain policy restriction.
	 * if found, the user is persist on database. Before the search on
	 * UserProvider, we check if user is not yet present in the database.
	 * 
	 * @param abstractDomain
	 * @param mail
	 *            : the user mail, not a fragment.
	 * @return a user, null if not found.
	 * @throws BusinessException
	 */
	private User findOrCreateUserWithoutRestriction(
			AbstractDomain abstractDomain, String mail)
			throws BusinessException {
		User user = userRepository.findByMailAndDomain(
				abstractDomain.getIdentifier(), mail);
		if (user == null) {
			// user was not found in database.
			// looking for it in userProvider (LDAP).
			user = abstractDomainService.findUserWithoutRestriction(
					abstractDomain, mail);
			if (user != null) {
				user = saveOrUpdateUser(user);
			}
		}
		return user;
	}

	@Override
	public User findOrCreateUserWithDomainPolicies(String domainId,
			String mail, String actorDomainId) throws BusinessException {
		Validate.notEmpty(mail, "domainmail is required");
		Validate.notEmpty(domainId, "domainId is required");
		User user = null;

		if (actorDomainId == null) {
			actorDomainId = domainId;
		}
		List<AbstractDomain> allAuthorizedDomains = abstractDomainService
				.getAllAuthorizedDomains(actorDomainId);
		AbstractDomain domain = abstractDomainService.findById(domainId);

		if (allAuthorizedDomains.contains(domain)) {
			user = findOrCreateUserWithoutRestriction(domain, mail);
		}

		// test if user was found
		if (user == null) {
			// User was not found in the given domain (parameter)
			// Now we search in all authorized domains.
			for (AbstractDomain abstractDomain : allAuthorizedDomains) {
				if (abstractDomain.equals(domain)) {
					// no need to search, already done.
					continue;
				}
				user = findOrCreateUserWithoutRestriction(abstractDomain, mail);
				if (user != null) {
					// We don't need to continue
					break;
				}
			}
		}

		if (user == null) {
			throw new BusinessException(BusinessErrorCode.USER_NOT_FOUND,
					"The user " + mail + " could not be found ! (domain id:"
							+ domainId + ", starting point:" + actorDomainId
							+ ")");
		}
		return user;
	}

	@Override
	public User findOrCreateUserWithDomainPolicies(String mail, String domainId)
			throws BusinessException {
		return findOrCreateUserWithDomainPolicies(domainId, mail, null);
	}

	@Override
	public User findOrCreateUser(String mail, String domainId)
			throws BusinessException {
		// AccountRepository<Account>
		User user = userRepository.findByMailAndDomain(domainId, mail);

		if (user == null) {
			List<User> users = abstractDomainService
					.searchUserRecursivelyWithoutRestriction(domainId, mail);
			if (users != null && users.size() == 1) {
				user = users.get(0);
				user = saveOrUpdateUser(user);
			} else {
				logger.error("Could not find the user " + mail
						+ " in the database nor in the LDAP");
				logger.debug("nb result : " + users.size());
				// this should really not happened
				throw new BusinessException(BusinessErrorCode.USER_NOT_FOUND,
						"The user could not be found in the DB nor in the LDAP");
			}
		}
		return user;
	}

	@Deprecated
	@Override
	public User searchAndCreateUserEntityFromDirectory(String domainIdentifier,
			String mail) throws BusinessException {

		logger.debug("domainIdentifier : " + domainIdentifier);
		logger.debug("mail : " + mail);
		// search user mail in in specific directory and all its SubDomain
		User userFound = abstractDomainService
				.searchOneUserRecursivelyWithoutRestriction(domainIdentifier,
						mail);

		if (userFound != null) {
			logger.debug("User '" + mail + "'found in domain : "
					+ userFound.getDomainId());
			saveOrUpdateUser(userFound);
			return userFound;
		} else if (logger.isDebugEnabled()) {
			logger.error("Impossible to create an user entity from domain : "
					+ domainIdentifier
					+ ". The searchUserRecursivelyWithoutRestriction method returns null.");
		}
		return null;
	}

	/**
	 * 
	 * New implementation created to not use old tapestry version. Destined to
	 * be more clear and to replace the old implementation when tapestry will be
	 * destroy.
	 * 
	 * 
	 */

	private User find(User tmpUser, String domainId) throws BusinessException {
		User user = null;
		if (tmpUser.getLsUuid() != null) {
			user = userRepository.findByLsUuid(tmpUser.getLsUuid());
		}
		if (user == null) {
			logger.debug("User "
					+ tmpUser.getMail()
					+ " was not found in the database. Searching in directories ...");
			user = this.findOrCreateUser(tmpUser.getMail(), domainId);
			if (user == null) {
				throw new TechnicalException(
						TechnicalErrorCode.USER_INCOHERENCE,
						"Couldn't find the user : " + tmpUser.getMail()
								+ " in domain : " + tmpUser.getDomainId());
			}
		}
		logger.debug("User " + tmpUser.getMail() + " found.");
		return user;
	}

	@Override
	public User updateUser(User actor, User updatedUser, String domainId)
			throws BusinessException {
		User user = find(updatedUser, domainId);
		Assert.notNull(updatedUser.getRole());

		user.setFirstName(updatedUser.getFirstName());
		user.setLastName(updatedUser.getLastName());
		if (!(updatedUser.getRole().equals(Role.SIMPLE) || updatedUser.getRole().equals(Role.ADMIN))) {
			throw new BusinessException(BusinessErrorCode.FORBIDDEN, "Role not authorized.");
		}
		user.setCanCreateGuest(updatedUser.getCanCreateGuest());
		user.setCanUpload(updatedUser.getCanUpload());
		user.setLocale(updatedUser.getLocale());
		user.setExternalMailLocale(updatedUser.getExternalMailLocale());
		if (user.isGuest()) {
			Guest updatedGuest = (Guest) updatedUser;
			Assert.notNull(updatedUser.getOwner());
			Assert.notNull(updatedGuest.getExpirationDate());
			Assert.isTrue(updatedUser.getCanCreateGuest() == false);
			Guest guest = (Guest) user;
			guest.setExpirationDate(updatedGuest.getExpirationDate());
			guest.setComment(updatedGuest.getComment());
			guest.setRestricted(updatedGuest.isRestricted());
			User owner = find((User) updatedGuest.getOwner(), updatedGuest
					.getOwner().getDomainId());
			guest.setOwner(owner);
		} else {
			// For internal users.
			user.setRole(updatedUser.getRole());
		}
		User update = userRepository.update(user);
		UserLogEntry logEntry = new UserLogEntry(actor, LogAction.USER_UPDATE,
				"Update of a user:" + user.getMail(), user);
		logEntryService.create(logEntry);
		return update;
	}

	@Override
	public List<User> findAllAccountsReadyToPurge()
			throws BusinessException {
		return userRepository.findAllAccountsReadyToPurge();
	}

	@Override
	public List<User> findAllDeletedAccountsToPurge(Date limit)
			throws BusinessException {
		return userRepository.findAllDeletedAccountsToPurge(limit);
	}
}
