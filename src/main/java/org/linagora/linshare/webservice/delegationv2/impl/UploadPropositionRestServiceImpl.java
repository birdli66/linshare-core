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
 * and free version of LinShare™, powered by Linagora © 2009-2018. Contribute to
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
package org.linagora.linshare.webservice.delegationv2.impl;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.linagora.linshare.core.facade.webservice.user.UploadPropositionFacade;
import org.linagora.linshare.mongo.entities.UploadProposition;
import org.linagora.linshare.webservice.delegationv2.UploadPropositionRestService;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/{actorUuid}/upload_propositions")
@Api(value = "/rest/delegation/v2/upload_propositions", description = "Upload Propositions API")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class UploadPropositionRestServiceImpl implements UploadPropositionRestService {

	private final UploadPropositionFacade uploadPropositionFacade;

	public UploadPropositionRestServiceImpl(UploadPropositionFacade uploadPropositionFacade) {
		super();
		this.uploadPropositionFacade = uploadPropositionFacade;
	}

	@GET
	@Path("/{uuid}")
	@ApiOperation(value = "Find an upload proposition.", response = UploadProposition.class)
	@ApiResponses({
			@ApiResponse(code = 403, message = "Current logged in account does not have the rights.") ,
			@ApiResponse(code = 404, message = "UploadProposition not found."),
			@ApiResponse(code = 400, message = "Bad request : missing required fields."),
			@ApiResponse(code = 500, message = "Internal server error."), })
	@Override
	public UploadProposition find(
			@ApiParam(value = "The actor (user) uuid.", required = true)
				@PathParam("actorUuid") String actorUuid,
			@ApiParam(value = "Upload proposition uuid", required = true)
				@PathParam(value = "uuid") String uuid) {
		return uploadPropositionFacade.find(actorUuid, uuid);
	}

	@GET
	@Path("/")
	@ApiOperation(value = "Find all upload propositions.", response = UploadProposition.class)
	@ApiResponses({
			@ApiResponse(code = 403, message = "Current logged in account does not have the rights.") ,
			@ApiResponse(code = 404, message = "UploadProposition not found."),
			@ApiResponse(code = 400, message = "Bad request : missing required fields."),
			@ApiResponse(code = 500, message = "Internal server error."), })
	@Override
	public List<UploadProposition> findAll(
			@ApiParam(value = "The actor (user) uuid.", required = true)
				@PathParam("actorUuid") String actorUuid) {
		return uploadPropositionFacade.findAllByAccountUuid(actorUuid);
	}
}
