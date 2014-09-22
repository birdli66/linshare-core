/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2014 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2009–2014. Contribute to
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

import java.util.List;
import java.util.Set;

import org.linagora.linshare.core.business.service.DocumentEntryBusinessService;
import org.linagora.linshare.core.domain.constants.LogAction;
import org.linagora.linshare.core.domain.constants.Role;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.Functionality;
import org.linagora.linshare.core.domain.entities.Thread;
import org.linagora.linshare.core.domain.entities.ThreadLogEntry;
import org.linagora.linshare.core.domain.entities.ThreadMember;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.rac.ThreadMemberResourceAccessControl;
import org.linagora.linshare.core.rac.ThreadResourceAccessControl;
import org.linagora.linshare.core.repository.ThreadMemberRepository;
import org.linagora.linshare.core.repository.ThreadRepository;
import org.linagora.linshare.core.service.FunctionalityReadOnlyService;
import org.linagora.linshare.core.service.LogEntryService;
import org.linagora.linshare.core.service.ThreadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadServiceImpl implements ThreadService {

	final private static Logger logger = LoggerFactory.getLogger(ThreadServiceImpl.class);

	private final ThreadRepository threadRepository;

	private final ThreadMemberRepository threadMemberRepository;

	private final DocumentEntryBusinessService documentEntryBusinessService;

	private final FunctionalityReadOnlyService functionalityReadOnlyService;

	private final LogEntryService logEntryService;

	private final ThreadResourceAccessControl threadAC;

	private final ThreadMemberResourceAccessControl threadMemberAC;

	public ThreadServiceImpl(
			ThreadRepository threadRepository,
			ThreadMemberRepository threadMemberRepository,
			DocumentEntryBusinessService documentEntryBusinessService,
			LogEntryService logEntryService,
			FunctionalityReadOnlyService functionalityService,
			ThreadResourceAccessControl threadResourceAccessControl,
			ThreadMemberResourceAccessControl threadMemberResourceAccessControl) {
		super();
		this.threadRepository = threadRepository;
		this.threadMemberRepository = threadMemberRepository;
		this.documentEntryBusinessService = documentEntryBusinessService;
		this.logEntryService = logEntryService;
		this.functionalityReadOnlyService = functionalityService;
		this.threadAC = threadResourceAccessControl;
		this.threadMemberAC = threadMemberResourceAccessControl;
	}

	@Override
	public Thread findByLsUuid(Account actor, Account owner, String uuid) {
		Thread thread = threadRepository.findByLsUuid(uuid);

		if (thread == null) {
			logger.error("Can't find thread  : " + uuid);
		}
		threadAC.checkReadPermission(actor, thread,
				BusinessErrorCode.THREAD_FORBIDDEN);
		return thread;
	}

	@Override
	public Thread findByLsUuidUnprotected(String uuid) {
		Thread thread = threadRepository.findByLsUuid(uuid);

		if (thread == null) {
			logger.error("Can't find thread  : " + uuid);
		}
		return thread;
	}

	@Override
	public List<Thread> findAll(Account actor, Account owner) {
		threadAC.checkListPermission(actor, owner, Thread.class,
				BusinessErrorCode.THREAD_FORBIDDEN);
		return threadRepository.findAll();
	}

	@Override
	public Thread create(Account actor, Account owner, String name) throws BusinessException {
		threadAC.checkCreatePermission(actor, owner, Thread.class,
				BusinessErrorCode.THREAD_FORBIDDEN);
		Functionality creation = functionalityReadOnlyService.getThreadCreationPermissionFunctionality(owner.getDomain());

		if (creation.getActivationPolicy().getStatus()){
			Thread thread = null;
			ThreadMember member = null;

			logger.debug("User " + owner.getAccountReprentation() + " trying to create new thread named " + name);
			thread = new Thread(owner.getDomain(), owner, name);
			threadRepository.create(thread);
			logEntryService.create(new ThreadLogEntry(owner, thread, LogAction.THREAD_CREATE, "Creation of a new thread."));

			// creator = first member = default admin
			member = new ThreadMember(true, true, (User) owner, thread);
			thread.getMyMembers().add(member);
			thread = threadRepository.update(thread);
			logEntryService.create(new ThreadLogEntry(owner, member, LogAction.THREAD_ADD_MEMBER,
					"Creating the first member of the newly created thread."));
			return thread;
		} else {
			logger.error("You can not create thread, you are not authorized.");
			logger.error("The current domain does not allow you to create a thread.");
			return null;
		}
	}

	@Override
	public ThreadMember getThreadMemberById(long id) throws BusinessException {
		return threadMemberRepository.findById(id);
	}

	@Override
	public ThreadMember getMemberFromUser(Thread thread, User user) throws BusinessException {
		return threadMemberRepository.findUserThreadMember(thread, user);
	}

	@Override
	public Set<ThreadMember> getMembers(Account actor, User owner, Thread thread)
			throws BusinessException {
		threadMemberAC.checkListPermission(actor, owner, ThreadMember.class,
				BusinessErrorCode.THREAD_MEMBER_FORBIDDEN);
		return thread.getMyMembers();
	}

	@Override
	public List<Thread> findAllWhereMember(User user) {
		return threadRepository.findAllWhereMember(user);
	}

	@Override
	public List<Thread> findAllWhereAdmin(User user) {
		return threadRepository.findAllWhereAdmin(user);
	}

	@Override
	public List<Thread> findAllWhereCanUpload(User user) {
		return threadRepository.findAllWhereCanUpload(user);
	}

	@Override
	public boolean hasAnyWhereAdmin(User user) {
		return threadMemberRepository.isUserAdminOfAny(user);
	}

	@Override
	public boolean isUserAdmin(User user, Thread thread) {
		return threadMemberRepository.isUserAdmin(user, thread);
	}

	@Override
	public int countMembers(Thread thread) {
		return threadMemberRepository.count(thread);
	}

	@Override
	public int countEntries(Thread thread) {
		return documentEntryBusinessService.countThreadEntries(thread);
	}

	@Override
	public ThreadMember addMember(Account actor, Account owner, Thread thread,
			User user, boolean admin, boolean canUpload)
			throws BusinessException {
		ThreadMember member = new ThreadMember(canUpload, admin, user, thread);

		threadMemberAC.checkCreatePermission(actor, owner, ThreadMember.class,
				BusinessErrorCode.THREAD_MEMBER_FORBIDDEN);
		if (getMemberFromUser(thread, user) != null) {
			logger.warn("The current " + user.getAccountReprentation()
					+ " user is already member of the thread : "
					+ thread.getAccountReprentation());
			throw new BusinessException(
					"You are not authorized to add member to this thread. Already exists.");
		}

		thread.getMyMembers().add(member);
		threadRepository.update(thread);

		logEntryService.create(new ThreadLogEntry(owner, member,
				LogAction.THREAD_ADD_MEMBER,
				"Adding a new member to a thread : "
						+ member.getUser().getAccountReprentation()));
		return member;
	}

	@Override
	public ThreadMember updateMember(Account actor, Account owner,
			ThreadMember member, boolean admin, boolean canUpload)
			throws BusinessException {
		threadMemberAC.checkUpdatePermission(actor, member,
				BusinessErrorCode.THREAD_MEMBER_FORBIDDEN);

		member.setAdmin(admin);
		member.setCanUpload(canUpload);
		return threadMemberRepository.update(member);
	}

	@Override
	public void deleteMember(Account actor, Account owner, Thread thread, ThreadMember member) throws BusinessException {
		threadMemberAC.checkDeletePermission(actor, owner, member,
				BusinessErrorCode.THREAD_MEMBER_FORBIDDEN);

		thread.getMyMembers().remove(member);
		threadRepository.update(thread);
		threadMemberRepository.delete(member);

		logEntryService.create(new ThreadLogEntry(owner, member,
				LogAction.THREAD_REMOVE_MEMBER,
				"Deleting a member in a thread."));
	}

	@Override
	public void deleteAllMembers(Account actor, Thread thread) throws BusinessException {
		// permission check
		checkUserIsAdmin(actor, thread);

		Object[] myMembers = thread.getMyMembers().toArray();

		for (Object threadMember : myMembers) {
			thread.getMyMembers().remove(threadMember);
			threadRepository.update(thread);
			threadMemberRepository.delete((ThreadMember) threadMember);
		}
		logEntryService.create(new ThreadLogEntry(actor, thread, LogAction.THREAD_REMOVE_MEMBER, "Deleting all members in a thread."));
	}

	@Override
	public void deleteAllUserMemberships(Account actor, User user) throws BusinessException {
		List<ThreadMember> memberships = threadMemberRepository.findAllUserMemberships(user);
		for (ThreadMember threadMember : memberships) {
			deleteMember(actor, actor, threadMember.getThread(), threadMember);
		}
	}

	@Override
	public void deleteThread(User actor, Account owner, Thread thread)
			throws BusinessException {
		threadAC.checkDeletePermission(actor, owner, thread,
				BusinessErrorCode.THREAD_FORBIDDEN);

		ThreadLogEntry log = new ThreadLogEntry(actor, thread,
				LogAction.THREAD_DELETE, "Deleting a thread.");
		// Delete all entries
		documentEntryBusinessService.deleteSetThreadEntry(thread.getEntries());
		thread.setEntries(null);
		threadRepository.update(thread);
		// Deleting members
		this.deleteAllMembers(actor, thread);
		// Deleting the thread
		threadRepository.delete(thread);
		logEntryService.create(log);
	}

	@Override
	public Thread rename(User actor, Account owner, Thread thread,
			String threadName) throws BusinessException {
		threadAC.checkUpdatePermission(actor, thread,
				BusinessErrorCode.THREAD_FORBIDDEN);

		String oldname = thread.getName();
		thread.setName(threadName);
		Thread update = threadRepository.update(thread);

		logEntryService.create(new ThreadLogEntry(actor, thread,
				LogAction.THREAD_RENAME, "Renamed thread from " + oldname
						+ " to " + threadName));
		return update;
	}

	@Override
	public List<Thread> findLatestWhereMember(User actor, int limit) {
		return threadRepository.findLatestWhereMember(actor, limit);
	}

	@Override
	public List<Thread> searchByName(User actor, String pattern) {
		return threadRepository.searchByName(actor, pattern);
	}

	@Override
	public List<Thread> searchByMembers(User actor, String pattern) {
		return threadRepository.searchAmongMembers(actor, pattern);
	}


    /* ***********************************************************
     *                   Helpers
     ************************************************************ */


	/**
	 * Check if actor is admin of the thread and so has the right to perform any action.
	 * Throw a BusinessException if the actor isn't authorized to modify the thread.
	 */
	private void checkUserIsAdmin(Account actor, Thread thread) throws BusinessException {
		if (actor.getRole().equals(Role.SUPERADMIN) || actor.getRole().equals(Role.SYSTEM)) {
			return; // superadmin or system accounts have all rights
		}
		if (!isUserAdmin((User) actor, thread)) {
			logger.error("Actor: " + actor.getAccountReprentation() + " isn't admin of the Thread: "
					+ thread.getAccountReprentation());
			throw new BusinessException(BusinessErrorCode.FORBIDDEN,
					"you are not authorized to perform this action on this thread.");
		}
	}
}