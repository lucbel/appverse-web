/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.appverse.web.framework.backend.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Application;

import org.appverse.web.framework.backend.api.helpers.log.AutowiredLogger;
import org.appverse.web.framework.backend.api.model.integration.IntegrationPaginatedDataFilter;
import org.appverse.web.framework.backend.rest.model.integration.SampleDTO;
import org.appverse.web.framework.backend.rest.services.integration.SampleRepository;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import server.org.appverse.service.rest.sample.ExtendedGrizzlyTestContainerFactory;
import server.org.appverse.service.rest.sample.SampleRestApplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class
 * 
 * We are implementing SampleRepository only to ensure every repository method is tested
 *
 */
@ContextConfiguration(locations = { "classpath:/spring/application-config.xml" })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class SampleGetTest extends JerseyTest {

	@Autowired
	@Qualifier("sampleRepositoryJson")
	SampleRepository jsonSampleRepository;

	@Autowired
	@Qualifier("sampleRepositoryXml")
	SampleRepository xmlSampleRepository;

	@AutowiredLogger
	private static Logger logger;

	@Before
	public void init()
	{
		//ApplicationContext appContext = new ClassPathXmlApplicationContext(
		//		"classpath:/spring/application-config.xml");

	}

	@Override
	protected Application configure() {
		enable(TestProperties.LOG_TRAFFIC);
		enable(TestProperties.DUMP_ENTITY);
		return SampleRestApplication.createApp();
	}

	@Override
	protected void configureClient(final ClientConfig config) {
		//config.register(SampleRestApplication.createMoxyJsonResolver());
		//config.register(ClientConfig.class).register(SampleRepositoryImpl.class);
	}

	@Override
	protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
		return new ExtendedGrizzlyTestContainerFactory();
	}

	@Test
	public void retrieveSample() throws Exception {
		SampleDTO dto = jsonSampleRepository.retrieveSample(3l);
		//final WebTarget resource = target().path("samples/1");
		//Response response = resource.request().get();
		assertNotNull(dto);
		assertEquals(dto.getId(), 3l);
		logger.info("retrieveSample OK");
	}

	@Test
	public void retrieveSamplesByFK() throws Exception {
		List<SampleDTO> dtos = jsonSampleRepository.retrieveSamples(67l);

		assertTrue(dtos != null && dtos.size() > 0);

		for (SampleDTO sample : dtos)
			assertEquals(sample.getForeignKey(), 67);
	}

	@Test
	public void retrieveSomeSamples() throws Exception {

		List<Long> ids = new ArrayList<Long>();
		ids.add(new Long(34));
		ids.add(new Long(56));
		ids.add(new Long(104));

		List<SampleDTO> dtos = jsonSampleRepository.retrieveSomeSamples(ids);
		assertNotNull(dtos);
		assertTrue(dtos.size() == 3);
		for (SampleDTO sample : dtos)
			assertTrue(sample.getId() == 34 || sample.getId() == 56 || sample.getId() == 104);
	}

	@Test
	public void retrieveSamples() throws Exception {

		List<SampleDTO> dtos = jsonSampleRepository.retrieveSamples();
		/*
				final WebTarget resource = target().path("samples");
				GenericType<List<SampleDTO>> genericType = new GenericType<List<SampleDTO>>() {
				};
				List<SampleDTO> dtos = resource.request(MediaType.APPLICATION_JSON).get(genericType);
		*/
		for (SampleDTO sample : dtos)
		{
			assertTrue(true);
		}

		assertNotNull(dtos);
		assertTrue(dtos.size() == 10);
	}

	@Test
	public void retrieveByFilter()
			throws Exception {

		IntegrationPaginatedDataFilter filter = new IntegrationPaginatedDataFilter();
		filter.getColumns().add("name");
		filter.getValues().add("John");
		List<SampleDTO> dtos = jsonSampleRepository.retrieveByFilter(filter);
		assertNotNull(dtos);
		assertTrue(dtos.size() > 0);
		for (SampleDTO sample : dtos)
			assertTrue(sample.getName() != null && sample.getName().equals("John"));
	}

	@Test
	public void retrieveOneByFilter()
			throws Exception {

		IntegrationPaginatedDataFilter filter = new IntegrationPaginatedDataFilter();
		filter.getColumns().add("surname");
		filter.getValues().add("Smith");
		SampleDTO sample = jsonSampleRepository.retrieveOneByFilter(filter);
		assertNotNull(sample);

		assertTrue(sample.getSurname() != null && sample.getSurname().equals("Smith"));
	}

	/*********************/
	/*  XML Test Methods */
	/*********************/

	@Test
	public void retrieveSampleXml() throws Exception {
		SampleDTO dto = xmlSampleRepository.retrieveSample(3l);
		//final WebTarget resource = target().path("samples/1");
		//Response response = resource.request().get();
		assertNotNull(dto);
		assertEquals(dto.getId(), 3l);
	}

	@Test
	public void retrieveSamplesByFKXml() throws Exception {
		List<SampleDTO> dtos = xmlSampleRepository.retrieveSamples(67l);

		assertTrue(dtos != null && dtos.size() > 0);

		for (SampleDTO sample : dtos)
			assertEquals(sample.getForeignKey(), 67);
	}

	@Test
	public void retrieveSomeSamplesXml() throws Exception {

		List<Long> ids = new ArrayList<Long>();
		ids.add(new Long(34));
		ids.add(new Long(56));
		ids.add(new Long(104));

		List<SampleDTO> dtos = xmlSampleRepository.retrieveSomeSamples(ids);
		assertNotNull(dtos);
		assertTrue(dtos.size() == 3);
		for (SampleDTO sample : dtos)
			assertTrue(sample.getId() == 34 || sample.getId() == 56 || sample.getId() == 104);
	}

	@Test
	public void retrieveSamplesXml() throws Exception {

		List<SampleDTO> dtos = xmlSampleRepository.retrieveSamples();
		/*
				final WebTarget resource = target().path("samples");
				GenericType<List<SampleDTO>> genericType = new GenericType<List<SampleDTO>>() {
				};
				List<SampleDTO> dtos = resource.request(MediaType.APPLICATION_JSON).get(genericType);
		*/
		for (SampleDTO sample : dtos)
		{
			assertTrue(true);
		}

		assertNotNull(dtos);
		assertTrue(dtos.size() == 10);
	}

	@Test
	public void retrieveByFilterXml()
			throws Exception {

		IntegrationPaginatedDataFilter filter = new IntegrationPaginatedDataFilter();
		filter.getColumns().add("name");
		filter.getValues().add("John");
		List<SampleDTO> dtos = xmlSampleRepository.retrieveByFilter(filter);
		assertNotNull(dtos);
		assertTrue(dtos.size() > 0);
		for (SampleDTO sample : dtos)
			assertTrue(sample.getName() != null && sample.getName().equals("John"));
	}

	@Test
	public void retrieveOneByFilterXml()
			throws Exception {

		IntegrationPaginatedDataFilter filter = new IntegrationPaginatedDataFilter();
		filter.getColumns().add("surname");
		filter.getValues().add("Smith");
		SampleDTO sample = xmlSampleRepository.retrieveOneByFilter(filter);
		assertNotNull(sample);

		assertTrue(sample.getSurname() != null && sample.getSurname().equals("Smith"));
	}

}
