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
 * and free version of LinShare™, powered by Linagora © 2009–2018. Contribute to
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
package org.linagora.linshare.webservice.admin.impl;

import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.facade.webservice.admin.GroupPatternFacade;
import org.linagora.linshare.core.facade.webservice.admin.dto.DomainPatternDto;
import org.linagora.linshare.core.facade.webservice.admin.dto.GroupLdapPatternDto;
import org.linagora.linshare.webservice.WebserviceBase;
import org.linagora.linshare.webservice.admin.GroupPatternRestService;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

@Path("/group_patterns")
@Api(value = "/rest/admin/group_patterns", description = "Group patterns service.")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public class GroupPatternRestServiceImpl extends WebserviceBase implements GroupPatternRestService {

	private final GroupPatternFacade groupPatternFacade;

	public GroupPatternRestServiceImpl(
			final GroupPatternFacade groupPatternFacade) {
		this.groupPatternFacade = groupPatternFacade;
	}

	@Path("/")
	@GET
	@ApiOperation(value = "Find all group patterns.", response = DomainPatternDto.class, responseContainer = "Set")
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have required permission."),
		@ApiResponse(code = 404, message = "GroupPattern not found."),
		@ApiResponse(code = 400, message = "Bad request : missing required fields."),
		@ApiResponse(code = 500, message = "Internal server error."), })
	@Override
	public Set<GroupLdapPatternDto> findAll() throws BusinessException {
		return groupPatternFacade.findAll();
	}

	@Path("/models")
	@GET
	@ApiOperation(value = "Find all groups pattern's  models.", response = DomainPatternDto.class, responseContainer = "Set")
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have required permission."),
		@ApiResponse(code = 404, message = "GroupPattern not found."),
		@ApiResponse(code = 400, message = "Bad request : missing required fields."),
		@ApiResponse(code = 500, message = "Internal server error."), })
	@Override
	public Set<GroupLdapPatternDto> findAllGroupPattern() throws BusinessException {
		return groupPatternFacade.findAll();
	}

	@Path("/{uuid}")
	@GET
	@ApiOperation(value = "Find a group pattern.", response = DomainPatternDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have required permission."),
		@ApiResponse(code = 404, message = "GroupPattern not found."),
		@ApiResponse(code = 400, message = "Bad request : missing required fields."),
		@ApiResponse(code = 500, message = "Internal server error."), })
	@Override
	public GroupLdapPatternDto find(
			@ApiParam(value = "group pattern uuid", required = true)
				@PathParam("uuid") String uuid) throws BusinessException {
		return groupPatternFacade.find(uuid);
	}

	@Path("/{uuid : .*}")
	@PUT
	@ApiOperation(value = "Update a group pattern.", response = DomainPatternDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have required permission."),
		@ApiResponse(code = 404, message = "GroupPattern not found."),
		@ApiResponse(code = 400, message = "Bad request : missing required fields."),
		@ApiResponse(code = 500, message = "Internal server error."), })
	@Override
	public GroupLdapPatternDto update(
			@ApiParam(value = "group pattern to update", required = true)
				GroupLdapPatternDto groupPattern,
			@ApiParam(value = "group pattern uuid", required = false)
				@PathParam("uuid") String uuid) throws BusinessException {
		return groupPatternFacade.update(groupPattern, uuid);
	}

	@Path("/")
	@POST
	@ApiOperation(value = "Create a group pattern.", response = DomainPatternDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have required permission."),
		@ApiResponse(code = 404, message = "GroupPattern not found."),
		@ApiResponse(code = 400, message = "Bad request : missing required fields."),
		@ApiResponse(code = 500, message = "Internal server error."), })
	@Override
	public GroupLdapPatternDto create(GroupLdapPatternDto groupPattern) throws BusinessException {
		return groupPatternFacade.create(groupPattern);
	}

	@Path("/{uuid : .*}")
	@DELETE
	@ApiOperation(value = "Delete a group pattern.", response = DomainPatternDto.class)
	@ApiResponses({ @ApiResponse(code = 403, message = "Current logged in account does not have required permission."),
		@ApiResponse(code = 404, message = "GroupPattern not found."),
		@ApiResponse(code = 400, message = "Bad request : missing required fields."),
		@ApiResponse(code = 500, message = "Internal server error."), })
	@Override
	public GroupLdapPatternDto delete(
			@ApiParam(value = "group pattern to delete", required = false)
			GroupLdapPatternDto groupPattern,
			@ApiParam(value = "group pattern uuid to delete (required if groupPattern is null)", required = false)
				@PathParam("uuid") String uuid) throws BusinessException {
		return groupPatternFacade.delete(groupPattern, uuid);
	}

}
