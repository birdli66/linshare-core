package org.linagora.linshare.core.service;

import java.io.InputStream;

import org.linagora.linshare.core.domain.constants.Reason;
import org.linagora.linshare.core.domain.entities.Account;
import org.linagora.linshare.core.domain.entities.DocumentEntry;
import org.linagora.linshare.core.domain.entities.User;
import org.linagora.linshare.core.exception.BusinessException;

public interface DocumentEntryService {
	
	/**
	 * Compute the MimeType from a file input stream 
	 * @param theFileStream
	 * @return
	 */
	public String getMimeType(InputStream theFileStream)  throws BusinessException;

	/**
	 * Insert a file in the path identifiable by its filename.
	 * @param fileName the name of the file which permits to identify it.
	 * @param owner : the user who uploads the document
	 * @param path the path inside the repository.
	 * @param file the stream content file.
	 * @return uuid the uuid of the inserted file.
	 * @throws BusinessException : FILE_TOO_LARGE if the file is too large to fit in user's space
	 */
	public DocumentEntry createDocumentEntry(Account actor, InputStream stream, Long size, String fileName) throws BusinessException;
	
	
	public DocumentEntry updateDocumentEntry(Account actor, Long currentDocEntryId, InputStream stream, Long size, String fileName) throws BusinessException ;
	
	public DocumentEntry duplicateDocumentEntry(Account actor, Long currentDocEntryId) throws BusinessException;
	
	public void deleteDocumentEntry(Account actor, Long currentDocEntryId, Reason causeOfDeletion) throws BusinessException;
	
	public long getUserMaxFileSize(Account account) throws BusinessException;
	
	public long getAvailableSize(Account account) throws BusinessException;
	
	public long getTotalSize(Account account) throws BusinessException ;
	
	
	 /**
     * Thumbnail of the document exists ?
     * @param uuid the identifier of the document
     * @return true if the thumbnail exists, false otherwise
     */
	public boolean documentHasThumbnail(Account owner, String uuid);
	
	 /**
     * Get the thumbnail (InputStream) of the document
     * @param uuid the identifier of the document
     * @return InputStream of the thumbnail
     */
    public InputStream getDocumentThumbnail(Account owner, String uuid);
    
    public InputStream getDocument(Account owner, String uuid);

	/**
	 * return true if the signature functionality is enabled
	 * @param user
	 * @return
	 */
	public boolean isSignatureActive(Account account);
	/**
	 * return true if the encipherment functionality is enabled
	 * @param user
	 * @return
	 */
	public boolean isEnciphermentActive(Account account);
	
	/**
	 * return true if the global quota functionality is enabled
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public boolean isGlobalQuotaActive(Account account) throws BusinessException;

	/**
	 * return true if the user quota functionality is enabled
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public boolean isUserQuotaActive(Account account) throws BusinessException;

	/**
	 * return the global quota value
	 * @param user
	 * @return
	 * @throws BusinessException
	 */
	public Long getGlobalQuota(Account account) throws BusinessException;
	
	public DocumentEntry findById(Account actor, Long id) throws BusinessException; 
	
	public void renameDocumentEntry(Account actor, Long id, String newName) throws BusinessException ;
	
	public void updateFileProperties(Account actor, Long id, String newName, String fileComment) throws BusinessException;
	
}
