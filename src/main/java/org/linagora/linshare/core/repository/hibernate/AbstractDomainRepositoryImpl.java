/*
 * LinShare is an open source filesharing software, part of the LinPKI software
 * suite, developed by Linagora.
 * 
 * Copyright (C) 2015-2018 LINAGORA
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
package org.linagora.linshare.core.repository.hibernate;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.linagora.linshare.core.domain.constants.DomainPurgeStepEnum;
import org.linagora.linshare.core.domain.constants.LinShareConstants;
import org.linagora.linshare.core.domain.entities.AbstractDomain;
import org.linagora.linshare.core.domain.entities.GuestDomain;
import org.linagora.linshare.core.domain.entities.MailConfig;
import org.linagora.linshare.core.domain.entities.SubDomain;
import org.linagora.linshare.core.domain.entities.TopDomain;
import org.linagora.linshare.core.domain.entities.WelcomeMessages;
import org.linagora.linshare.core.exception.BusinessErrorCode;
import org.linagora.linshare.core.exception.BusinessException;
import org.linagora.linshare.core.repository.AbstractDomainRepository;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.util.Assert;

public class AbstractDomainRepositoryImpl extends
		AbstractRepositoryImpl<AbstractDomain> implements
		AbstractDomainRepository {

	public AbstractDomainRepositoryImpl(HibernateTemplate hibernateTemplate) {
		super(hibernateTemplate);
	}

	@Override
	protected DetachedCriteria getNaturalKeyCriteria(AbstractDomain entity) {
		DetachedCriteria det = DetachedCriteria.forClass(getPersistentClass())
				.add(Restrictions.eq("uuid", entity.getUuid()));
		return det;
	}

	@Override
	public List<AbstractDomain> findAll() {
		return findByCriteria(DetachedCriteria.forClass(getPersistentClass())
				.add(Restrictions.eq("purgeStep", DomainPurgeStepEnum.IN_USE)));
	}

	@Override
	public AbstractDomain findById(String identifier) {
		return DataAccessUtils.singleResult(findByCriteria(
				Restrictions.eq("uuid", identifier)));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllDomainIdentifiers() {
		DetachedCriteria crit = DetachedCriteria.forClass(getPersistentClass())
				.setProjection(Projections.property("uuid"))
				.addOrder(Order.asc("authShowOrder"));
		crit.add(Restrictions.eq("purgeStep", DomainPurgeStepEnum.IN_USE));
		return listByCriteria(crit);
	}

	@Override
	public List<AbstractDomain> findAllDomain() {
		return findByCriteria(DetachedCriteria.forClass(getPersistentClass())
				.addOrder(Order.asc("authShowOrder"))
				.add(Restrictions.eq("purgeStep", DomainPurgeStepEnum.IN_USE)));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AbstractDomain> findAllTopAndSubDomain() {
		DetachedCriteria crit = DetachedCriteria.forClass(getPersistentClass());
		crit.add(Restrictions.disjunction()
				.add(Restrictions.eq("class", TopDomain.class))
				.add(Restrictions.eq("class", SubDomain.class)))
			.add(Restrictions.eq("purgeStep", DomainPurgeStepEnum.IN_USE));
		return listByCriteria(crit);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllGuestAndSubDomainIdentifiers() {
		DetachedCriteria crit = DetachedCriteria.forClass(getPersistentClass());
		crit.setProjection(Projections.property("uuid"));
		crit.add(Restrictions.disjunction()
				.add(Restrictions.eq("class", GuestDomain.class))
				.add(Restrictions.eq("class", SubDomain.class)))
			.add(Restrictions.eq("purgeStep", DomainPurgeStepEnum.IN_USE));
		return listByCriteria(crit);
	}

	@Override
	public List<AbstractDomain> findAllTopDomain() {
		return findByCriteria(DetachedCriteria.forClass(getPersistentClass())
				.add(Restrictions.eq("class", TopDomain.class))
				.add(Restrictions.eq("purgeStep", DomainPurgeStepEnum.IN_USE)));
	}

	@Override
	public List<AbstractDomain> findAllSubDomain() {
		return findByCriteria(DetachedCriteria.forClass(getPersistentClass())
				.add(Restrictions.eq("class", SubDomain.class))
				.add(Restrictions.eq("purgeStep", DomainPurgeStepEnum.IN_USE)));
	}

	@Override
	public List<AbstractDomain> findByCurrentMailConfig(MailConfig cfg) {
		return findByCriteria(DetachedCriteria.forClass(getPersistentClass())
				.add(Restrictions.eq("currentMailConfiguration", cfg))
				.add(Restrictions.eq("purgeStep", DomainPurgeStepEnum.IN_USE)));
	}

	@Override
	public AbstractDomain getUniqueRootDomain() throws BusinessException {
		AbstractDomain domain = this.findById(LinShareConstants.rootDomainIdentifier);
		if (domain == null) {
			throw new BusinessException(
					BusinessErrorCode.DATABASE_INCOHERENCE_NO_ROOT_DOMAIN,
					"No root domain found in the database.");
		}
		return domain;
	}

	@Override
	public List<AbstractDomain> loadDomainsForAWelcomeMessage(
			WelcomeMessages welcomeMessage) throws BusinessException {
		return findByCriteria(Restrictions.eq("currentWelcomeMessage",
				welcomeMessage));
	}

	@Override
	public List<String> getAllSubDomainIdentifiers(String domain) {
		DetachedCriteria det = DetachedCriteria.forClass(getPersistentClass());
		det.createAlias("parentDomain", "parent");
		det.add(Restrictions.eq("parent.uuid", domain));
		det.add(Restrictions.eq("purgeStep", DomainPurgeStepEnum.IN_USE));
		det.setProjection(Projections.property("uuid"));
		@SuppressWarnings("unchecked")
		List<String> ret = (List<String>)listByCriteria(det);
		return ret;
	}

	@Override
	public AbstractDomain create(AbstractDomain entity) throws BusinessException {
		entity.setUuid(UUID.randomUUID().toString());
		entity.setCreationDate(new Date());
		entity.setModificationDate(new Date());
		return super.create(entity);
	}

	@Override
	public AbstractDomain update(AbstractDomain entity) throws BusinessException {
		entity.setModificationDate(new Date());
		return super.update(entity);
	}

	@Override
	public void markToPurge(AbstractDomain abstractDomain) throws BusinessException, IllegalArgumentException {
		abstractDomain.setPurgeStep(DomainPurgeStepEnum.WAIT_FOR_PURGE);
		abstractDomain.setModificationDate(new Date());
		this.update(abstractDomain);
	}

	@Override
	public void purge(AbstractDomain abstractDomain) throws BusinessException, IllegalArgumentException {
		abstractDomain.setPurgeStep(DomainPurgeStepEnum.PURGED);
		abstractDomain.setModificationDate(new Date());
		this.update(abstractDomain);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllAbstractDomainsReadyToPurge() {
		DetachedCriteria criteria = DetachedCriteria.forClass(getPersistentClass());
		criteria.setProjection(Projections.property("uuid"));
		criteria.add(Restrictions.eq("purgeStep", DomainPurgeStepEnum.WAIT_FOR_PURGE));
		return listByCriteria(criteria);
	}

	@Override
	public AbstractDomain findDomainReadyToPurge(String lsUuid) {
		Assert.notNull(lsUuid);
		DetachedCriteria criteria = DetachedCriteria.forClass(getPersistentClass());
		criteria.add(Restrictions.eq("purgeStep", DomainPurgeStepEnum.WAIT_FOR_PURGE));
		criteria.add(Restrictions.eq("uuid", lsUuid).ignoreCase());
		return DataAccessUtils.requiredSingleResult(findByCriteria(criteria));
	}
	
	@Override
	public List<AbstractDomain> getSubDomainsByDomain(String domain) {
		DetachedCriteria det = DetachedCriteria.forClass(getPersistentClass());
		det.createAlias("parentDomain", "parent");
		det.add(Restrictions.eq("parent.uuid", domain));
		det.add(Restrictions.eq("purgeStep", DomainPurgeStepEnum.IN_USE));
		return findByCriteria(det);
	}

	@Override
	public AbstractDomain getGuestSubDomainByDomain(String uuid) {
		DetachedCriteria det = DetachedCriteria.forClass(GuestDomain.class);
		det.createAlias("parentDomain", "parent");
		det.add(Restrictions.eq("parent.uuid", uuid));
		det.add(Restrictions.eq("purgeStep", DomainPurgeStepEnum.IN_USE));
		return DataAccessUtils.singleResult(findByCriteria(det));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> findAllDomainIdentifiersWithGroupProviders() {
		DetachedCriteria crit = DetachedCriteria.forClass(getPersistentClass())
				.setProjection(Projections.property("uuid"))
				.addOrder(Order.asc("authShowOrder"));
		crit.add(Restrictions.eq("purgeStep", DomainPurgeStepEnum.IN_USE));
		crit.add(Restrictions.isNotNull("groupProvider"));
		return listByCriteria(crit);
	}
}
