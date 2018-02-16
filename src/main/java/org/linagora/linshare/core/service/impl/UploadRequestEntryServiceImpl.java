/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2018 LINAGORA
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for
 * LinShare software by Linagora pursuant to Section 7 of the GNU Affero General
 * Public License, subsections (b), (c), and (e), pursuant to which you must
 * notably (i) retain the display of the “LinShare™” trademark/logo at the top
 * of the interface window, the display of the “You are using the Open Source
 * and free version of LinShare™, powered by Linagora © 2018. Contribute to
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

import java.io.File;
import java.io.InputStream;
import java.util.Calendar;

import org.apache.commons.lang.Validate;
import org.linagora.linshare.core.business.service.OperationHistoryBusinessService;
import org.linagora.linshare.core.business.service.UploadRequestEntryBusinessService;
import org.linagora.linshare.core.dao.MimeTypeMagicNumberDao;
import org.linagora.linshare.core.domain.constants.ContainerQuotaType;
import org.linagora.linshare.core.domain.constants.OperationHistoryTypeEnum;
import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.DocumentEntry;
import org.linagora.linshare.core.domain.entities.Functionality;
import org.linagora.linshare.core.domain.entities.OperationHistory;
import org.linagora.linshare.core.domain.entities.StringValueFunctionality;
import org.linagora.linshare.core.domain.entities.UploadRequestEntry;
import org.linagora.linshare.core.domain.entities.UploadRequestUrl;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.rac.UploadRequestEntryRessourceAccessControl;
import org.linagora.linshare.core.service.AbstractDomainService;
import org.linagora.linshare.core.service.AntiSamyService;
import org.linagora.linshare.core.service.FunctionalityReadOnlyService;
import org.linagora.linshare.core.service.MimeTypeService;
import org.linagora.linshare.core.service.QuotaService;
import org.linagora.linshare.core.service.UploadRequestEntryService;
import org.linagora.linshare.core.service.VirusScannerService;

public class UploadRequestEntryServiceImpl extends GenericEntryServiceImpl<Account, UploadRequestEntry>
		implements UploadRequestEntryService {

	private final UploadRequestEntryBusinessService uploadRequestEntryBusinessService;

	private final OperationHistoryBusinessService operationHistoryBusinessService;

	private final AbstractDomainService abstractDomainService;

	private final FunctionalityReadOnlyService functionalityReadOnlyService;

	private final MimeTypeService mimeTypeService;

	private final VirusScannerService virusScannerService;

	private final MimeTypeMagicNumberDao mimeTypeIdentifier;

	private final AntiSamyService antiSamyService;

	private final QuotaService quotaService;

	public UploadRequestEntryServiceImpl(
			UploadRequestEntryBusinessService uploadRequestEntryBusinessService,
			AbstractDomainService abstractDomainService,
			FunctionalityReadOnlyService functionalityReadOnlyService,
			MimeTypeService mimeTypeService,
			VirusScannerService virusScannerService,
			MimeTypeMagicNumberDao mimeTypeIdentifier,
			AntiSamyService antiSamyService,
			UploadRequestEntryRessourceAccessControl rac,
			OperationHistoryBusinessService operationHistoryBusinessService,
			QuotaService quotaService) {
		super(rac);
		this.uploadRequestEntryBusinessService = uploadRequestEntryBusinessService;
		this.abstractDomainService = abstractDomainService;
		this.functionalityReadOnlyService = functionalityReadOnlyService;
		this.mimeTypeService = mimeTypeService;
		this.virusScannerService = virusScannerService;
		this.mimeTypeIdentifier = mimeTypeIdentifier;
		this.antiSamyService = antiSamyService;		
		this.operationHistoryBusinessService = operationHistoryBusinessService;
		this.quotaService = quotaService;
	}

	@Override
	public UploadRequestEntry create(Account actor, Account owner, File tempFile, String fileName, String comment,
			boolean isFromCmis, String metadata, UploadRequestUrl uploadRequestUrl) throws BusinessException {
		preChecks(actor, owner);
		Validate.notEmpty(fileName, "fileName is required.");
		UploadRequestEntry upReqEntry = null;
		try {
			fileName = sanitizeFileName(fileName);
			Long size = tempFile.length();
			checkSpace(owner, size);

			// detect file's mime type.
			String mimeType = mimeTypeIdentifier.getMimeType(tempFile);

			// check if the file MimeType is allowed
			if (mimeTypeFilteringStatus(owner)) {
				mimeTypeService.checkFileMimeType(owner, fileName, mimeType);
			}

			virusScannerService.checkVirus(fileName, owner, tempFile, size);

			// want a timestamp on doc ?
			String timeStampingUrl = null;
			StringValueFunctionality timeStampingFunctionality = functionalityReadOnlyService
					.getTimeStampingFunctionality(owner.getDomain());
			if (timeStampingFunctionality.getActivationPolicy().getStatus()) {
				timeStampingUrl = timeStampingFunctionality.getValue();
			}

			Functionality enciphermentFunctionality = functionalityReadOnlyService
					.getEnciphermentFunctionality(owner.getDomain());
			Boolean checkIfIsCiphered = enciphermentFunctionality.getActivationPolicy().getStatus();

			// We need to set an expiration date in case of file cleaner
			// activation.
			upReqEntry = uploadRequestEntryBusinessService.createUploadRequestEntryDocument(owner, tempFile, size,
					fileName, comment, checkIfIsCiphered, timeStampingUrl, mimeType,
					getDocumentExpirationDate(owner.getDomain()), isFromCmis, metadata, uploadRequestUrl);
			addToQuota(owner, size);
		} finally {
			try {
				logger.debug("deleting temp file : " + tempFile.getName());
				if (tempFile.exists()) {
					tempFile.delete(); // remove the temporary file
				}
			} catch (Exception e) {
				logger.error("can not delete temp file : " + e.getMessage(),e);
			}
		}
		return upReqEntry;
	}

		@Override
		public boolean mimeTypeFilteringStatus(Account actor) throws BusinessException {
			AbstractDomain domain = abstractDomainService.retrieveDomain(actor.getDomain().getUuid());
			Functionality mimeFunctionality = functionalityReadOnlyService.getMimeTypeFunctionality(domain);
			return mimeFunctionality.getActivationPolicy().getStatus();
		}

		private String sanitizeFileName(String fileName) throws BusinessException {
			fileName = fileName.replace("\\", "_");
			fileName = fileName.replace(":", "_");
			fileName = antiSamyService.clean(fileName);
			if (fileName.isEmpty()) {
				throw new BusinessException(BusinessErrorCode.INVALID_FILENAME,
						"fileName is empty after the xss filter");
			}
			return fileName;
		}

		protected void checkSpace(Account owner, long size) throws BusinessException {
			quotaService.checkIfUserCanAddFile(owner, size, ContainerQuotaType.USER);
		}

		protected void addToQuota(Account owner, Long size) {
			OperationHistory oh = new OperationHistory(owner, owner.getDomain(), size, OperationHistoryTypeEnum.CREATE,
					ContainerQuotaType.USER);
			operationHistoryBusinessService.create(oh);
		}

		protected Calendar getDocumentExpirationDate(AbstractDomain domain) {
			return functionalityReadOnlyService.getDefaultFileExpiryTime(domain);
		}

	@Override
	public UploadRequestEntry find(Account authUser, Account actor, String uuid) {
		preChecks(authUser, actor);
		return uploadRequestEntryBusinessService.findByUuid(uuid);
	}
	
	@Override
	public InputStream getDocumentStream(Account actor, Account owner, String uuid) throws BusinessException {
		preChecks(actor, owner);
		Validate.notEmpty(uuid, "upload request entry uuid is required.");
		UploadRequestEntry entry = find(actor, owner, uuid);
		checkDownloadPermission(actor, owner, DocumentEntry.class, BusinessErrorCode.DOCUMENT_ENTRY_FORBIDDEN, entry);
		// TODO log
		return uploadRequestEntryBusinessService.getDocumentStream(entry);
	}
}