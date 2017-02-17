/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2016 LINAGORA
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
package org.linagora.linshare.mongo.entities.logs;

import javax.xml.bind.annotation.XmlRootElement;

import org.linagora.linshare.core.domain.constants.AuditLogEntryType;
import org.linagora.linshare.core.domain.constants.LogAction;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.Thread;
import org.linagora.linshare.core.domain.entities.ThreadEntry;
import org.linagora.linshare.core.facade.webservice.common.dto.WorkGroupLightDto;
import org.linagora.linshare.mongo.entities.WorkGroupEntry;
import org.linagora.linshare.mongo.entities.mto.AccountMto;

@XmlRootElement
public class WorkGroupEntryAuditLogEntry extends AuditLogEntryUser {

	protected WorkGroupLightDto workGroup;

	protected WorkGroupEntry resource;

	private WorkGroupEntry resourceUpdated;

	public WorkGroupEntryAuditLogEntry() {
		super();
	}

	public WorkGroupEntryAuditLogEntry(Account actor, Account owner, LogAction action, AuditLogEntryType type,
			ThreadEntry threadEntry) {
		super(new AccountMto(actor), new AccountMto(owner), action, type, threadEntry.getUuid());
		this.resource = new WorkGroupEntry(threadEntry, new AccountMto(owner));
		Thread workGroup = (Thread) threadEntry.getEntryOwner();
		this.workGroup = new WorkGroupLightDto(workGroup);
	}

	public WorkGroupEntry getResource() {
		return resource;
	}

	public void setResource(WorkGroupEntry resource) {
		this.resource = resource;
	}

	public WorkGroupEntry getResourceUpdated() {
		return resourceUpdated;
	}

	public void setResourceUpdated(WorkGroupEntry resourceUpdated) {
		this.resourceUpdated = resourceUpdated;
	}

	public void setResourceUpdated(ThreadEntry threadEntry, Account owner) {
		this.resource = new WorkGroupEntry(threadEntry, new AccountMto(owner));
	}

	public WorkGroupLightDto getWorkGroup() {
		return workGroup;
	}

	public void setWorkGroup(WorkGroupLightDto workGroup) {
		this.workGroup = workGroup;
	}
}