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
package org.linagora.linshare.core.domain.entities;

import java.util.Date;
import java.util.Set;

public class UploadRequest {

	private long id;

	private UploadRequestGroup uploadRequestGroup;

	private String uuid;

	private int maxFileCount;

	private long maxDepositSize;

	private long maxFileSize;

	private int status;

	private Date activationDate;

	private Date creationDate;

	private Date modificationDate;

	private Date notificationDate;

	private Date expiryDate;

	private Integer uploadPropositionRequestUuid;

	private boolean canDelete;

	private boolean canClose;

	private boolean canEditExpiryDate;

	private String locale;

	private boolean wasSecured;

	private String mailMessageID;

	private Set<UploadRequestUrl> UploadRequestURL;

	private Set<UploadRequestEntry> uploadRequestEntries;

	private Set<UploadRequestHistory> uploadRequestHistory;

	public UploadRequest() {
		super();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public UploadRequestGroup getUploadRequestGroup() {
		return uploadRequestGroup;
	}

	public void setUploadRequestGroup(
			UploadRequestGroup uploadRequestGroup) {
		this.uploadRequestGroup = uploadRequestGroup;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getMaxFileCount() {
		return maxFileCount;
	}

	public void setMaxFileCount(int maxFileCount) {
		this.maxFileCount = maxFileCount;
	}

	public long getMaxDepositSize() {
		return maxDepositSize;
	}

	public void setMaxDepositSize(long maxDepositSize) {
		this.maxDepositSize = maxDepositSize;
	}

	public long getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	public Date getNotificationDate() {
		return notificationDate;
	}

	public void setNotificationDate(Date notificationDate) {
		this.notificationDate = notificationDate;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public Integer getUploadPropositionRequestUuid() {
		return uploadPropositionRequestUuid;
	}

	public void setUploadPropositionRequestUuid(Integer uploadPropositionRequestUuid) {
		this.uploadPropositionRequestUuid = uploadPropositionRequestUuid;
	}

	public boolean isCanDelete() {
		return canDelete;
	}

	public void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
	}

	public boolean isCanClose() {
		return canClose;
	}

	public void setCanClose(boolean canClose) {
		this.canClose = canClose;
	}

	public boolean isCanEditExpiryDate() {
		return canEditExpiryDate;
	}

	public void setCanEditExpiryDate(boolean canEditExpiryDate) {
		this.canEditExpiryDate = canEditExpiryDate;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public boolean isWasSecured() {
		return wasSecured;
	}

	public void setWasSecured(boolean wasSecured) {
		this.wasSecured = wasSecured;
	}

	public String getMailMessageID() {
		return mailMessageID;
	}

	public void setMailMessageID(String mailMessageID) {
		this.mailMessageID = mailMessageID;
	}

	public Set<UploadRequestUrl> getUploadRequestURL() {
		return UploadRequestURL;
	}

	public void setUploadRequestURL(Set<UploadRequestUrl> uploadRequestURL) {
		UploadRequestURL = uploadRequestURL;
	}

	public Set<UploadRequestEntry> getUploadRequestEntries() {
		return uploadRequestEntries;
	}

	public void setUploadRequestEntries(Set<UploadRequestEntry> uploadRequestEntries) {
		this.uploadRequestEntries = uploadRequestEntries;
	}

	public Set<UploadRequestHistory> getUploadRequestHistory() {
		return uploadRequestHistory;
	}

	public void setUploadRequestHistory(
			Set<UploadRequestHistory> uploadRequestHistory) {
		this.uploadRequestHistory = uploadRequestHistory;
	}
}