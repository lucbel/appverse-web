/*
 Copyright (c) 2012 GFT Appverse, S.L., Sociedad Unipersonal.

 This Source Code Form is subject to the terms of the Appverse Public License 
 Version 2.0 (“APL v2.0”). If a copy of the APL was not distributed with this 
 file, You can obtain one at http://www.appverse.mobi/licenses/apl_v2.0.pdf. [^]

 Redistribution and use in source and binary forms, with or without modification, 
 are permitted provided that the conditions of the AppVerse Public License v2.0 
 are met.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. EXCEPT IN CASE OF WILLFUL MISCONDUCT OR GROSS NEGLIGENCE, IN NO EVENT
 SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT(INCLUDING NEGLIGENCE OR OTHERWISE) 
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 POSSIBILITY OF SUCH DAMAGE.
 */

package org.appverse.web.framework.backend.rest.services.integration.impl.live;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.appverse.web.framework.backend.api.helpers.log.AutowiredLogger;
import org.appverse.web.framework.backend.api.model.integration.AbstractIntegrationBean;
import org.appverse.web.framework.backend.api.model.integration.IntegrationPaginatedDataFilter;
import org.appverse.web.framework.backend.api.services.integration.AbstractIntegrationService;
import org.appverse.web.framework.backend.api.services.integration.IntegrationException;
import org.appverse.web.framework.backend.api.services.integration.ServiceUnavailableException;
import org.appverse.web.framework.backend.rest.model.integration.IntegrationPaginatedResult;
import org.appverse.web.framework.backend.rest.model.integration.StatusResult;
import org.appverse.web.framework.backend.rest.services.integration.IRestPersistenceService;
import org.slf4j.Logger;

/**
 * This class provides integration with Rest services 
 * 
 */
public abstract class RestPersistenceService<T extends AbstractIntegrationBean> extends
		AbstractIntegrationService<T>
		implements IRestPersistenceService<T> {

	public static final String MAX_RECORDS_PARAM_NAME = "maxRecords";
	public static final String OFFSET_PARAM_NAME = "offset";

	//	@Autowired
	//	private ObjectMapper objectMapper;

	@AutowiredLogger
	private static Logger logger;

	@Override
	public T retrieve(final WebTarget webClient, final String idName, final Long id)
			throws Exception {
		return retrieve(webClient, idName, id, null, null);
	}

	@Override
	public T retrieve(final WebTarget webClient, final Map<String, Object> pathParams,
			final Map<String, Object> queryParams)
			throws Exception {
		return this.retrieve(webClient, null, null, pathParams, queryParams);
	}

	@Override
	public T retrieve(WebTarget webClient, final String idName, final Long id,
			final Map<String, Object> pathParams,
			final Map<String, Object> queryParams)
			throws Exception {

		if (queryParams != null)
			webClient = applyQuery(webClient, queryParams);

		GenericType<T> genericType = new GenericType<T>(getClassP()) {
		};

		if (id != null && idName != null)
			webClient = webClient.resolveTemplate(idName, id);

		if (pathParams != null)
			webClient = webClient.resolveTemplates(pathParams);

		Builder builder = webClient.request();
		T object = acceptMediaType(builder).get(genericType);

		return object;
	}

	@Override
	public List<T> retrieveList(final WebTarget webClient, final String idName, final Long id)
			throws Exception {
		return retrieveList(webClient, idName, id, null, null);
	}

	@Override
	public List<T> retrieveList(WebTarget webClient, final String idName, final Long id,
			final Map<String, Object> pathParams, final Map<String, Object> queryParams)
			throws Exception {

		if (queryParams != null)
			webClient = applyQuery(webClient, queryParams);

		//We need jackson json unmarshaller, since Moxy is unable to deserialize generics collections List<T>
		GenericType<List<T>> genericType = new GenericType<List<T>>(getTypeListP()) {
		};

		if (pathParams != null)
			webClient = webClient.resolveTemplates(pathParams);

		if (id != null && idName != null)
			webClient = webClient.resolveTemplate(idName, id);

		Builder builder = webClient.request();
		List<T> objects = acceptMediaType(builder).get(genericType);
		return objects;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gft.riaframework.backend.services.integration.IRestPersistenceService
	 * #retrieveList()
	 */
	@Override
	public List<T> retrieveList(final WebTarget webClient) throws Exception {
		return retrieveList(webClient, (Map<String, Object>) null, (Map<String, Object>) null);
	}

	@Override
	public List<T> retrieveList(final WebTarget webClient, final Map<String, Object> pathParams,
			final Map<String, Object> queryParams)
			throws Exception {
		return retrieveList(webClient, (String) null, (Long) null, pathParams, queryParams);
	}

	@Override
	public List<T> retrieveList(final WebTarget webClient, final String idsName,
			final List<Long> ids)
			throws Exception {
		return retrieveList(webClient, idsName, ids, null, null);
	}

	@Override
	public List<T> retrieveList(WebTarget webClient, final String idsName,
			final List<Long> ids, final Map<String, Object> pathParams,
			final Map<String, Object> queryParams) throws Exception {

		String idsString = StringUtils.join(ids, ',');
		webClient = webClient.resolveTemplate(idsName, idsString);
		return retrieveList(webClient, pathParams, queryParams);

	}

	@Override
	public IntegrationPaginatedResult<T> retrievePagedQuery(final WebTarget webClient,
			final IntegrationPaginatedDataFilter filter)
			throws Exception {

		return retrievePagedQuery(webClient, filter, null, null);
	}

	@Override
	public IntegrationPaginatedResult<T> retrievePagedQuery(WebTarget webClient,
			final IntegrationPaginatedDataFilter filter, final Map<String, Object> pathParams,
			final Map<String, Object> queryParams)
			throws Exception {

		if (queryParams != null)
			webClient = applyQuery(webClient, queryParams);

		Response resp = null;
		try {

			if (pathParams != null)
				webClient = webClient.resolveTemplates(pathParams);

			Builder builder = webClient.queryParam(getOffsetParamName(), filter.getOffset())
					.queryParam(getMaxRecordsParamName(), filter.getLimit()).request();

			resp = acceptMediaType(builder).get();

		} catch (Exception exc) {
			logger.error("The service threw an exception at call{" + webClient.getUri()
					+ "} . Response status: " + resp.getStatus());
			throw new IntegrationException(exc);
		}
		IntegrationPaginatedResult<T> result = new IntegrationPaginatedResult<T>(
				Collections.<T> emptyList(), 0, 0);

		if (Status.OK.getStatusCode() == resp.getStatus()) {
			result = mapPagedResult(resp);
		} else if (Status.SERVICE_UNAVAILABLE.getStatusCode() == resp.getStatus()) {
			logger.error("Problem with call {" + webClient.getUri()
					+ "} . Response status: " + resp.getStatus());
			//Appverse integration exception
			throw new ServiceUnavailableException();
		} else {
			logger.error("Problem with call {" + webClient.getUri()
					+ "} . Response status: " + resp.getStatus());
		}

		return result;
	}

	// DELETE METHODS

	@Override
	public StatusResult deleteStatusReturn(final WebTarget webClient, final String idName,
			final Long id)
			throws Exception
	{
		return deleteStatusReturn(webClient, idName, id, null, null);
	}

	@Override
	public StatusResult deleteStatusReturn(WebTarget webClient, final String idName, final Long id,
			final Map<String, Object> pathParams,
			final Map<String, Object> queryParams) throws Exception
	{
		if (queryParams != null)
			webClient = applyQuery(webClient, queryParams);

		if (id != null && idName != null)
			webClient = webClient.resolveTemplate(idName, id);

		if (pathParams != null)
			webClient = webClient.resolveTemplates(pathParams);

		Builder builder = webClient.request();

		Response resp = acceptMediaType(builder).delete();
		return getStatusResult(resp);

	}

	@Override
	public T delete(final WebTarget webClient, final String idName, final Long id) throws Exception
	{
		return delete(webClient, idName, id, null, null);
	}

	@Override
	public T delete(WebTarget webClient, final String idName, final Long id,
			final Map<String, Object> pathParams,
			final Map<String, Object> queryParams) throws Exception
	{
		if (queryParams != null)
			webClient = applyQuery(webClient, queryParams);

		GenericType<T> genericType = new GenericType<T>(getClassP()) {
		};

		if (id != null && idName != null)
			webClient = webClient.resolveTemplate(idName, id);

		if (pathParams != null)
			webClient = webClient.resolveTemplates(pathParams);

		Builder builder = acceptMediaType(webClient.request());
		return builder.delete(genericType);
	}

	//INSERT METHODS

	@Override
	public T insert(final WebTarget webClient, final T object) throws Exception {
		return insert(webClient, object, null, null);
	}

	@Override
	public T insert(WebTarget webClient, final T object,
			final Map<String, Object> pathParams,
			final Map<String, Object> queryParams) throws Exception {

		if (queryParams != null)
			webClient = applyQuery(webClient, queryParams);

		GenericType<T> genericType = new GenericType<T>(getClassP()) {
		};

		if (pathParams != null)
			webClient = webClient.resolveTemplates(pathParams);

		Builder builder = acceptMediaType(webClient.request());
		return builder.post(Entity.entity(object, getMediaType()), genericType);
	}

	@Override
	public StatusResult insertStatusReturn(final WebTarget webClient, final T object)
			throws Exception {
		return insertStatusReturn(webClient, object, null, null);
	}

	@Override
	public StatusResult insertStatusReturn(WebTarget webClient, final T object,
			final Map<String, Object> pathParams,
			final Map<String, Object> queryParams) throws Exception {

		if (queryParams != null)
			webClient = applyQuery(webClient, queryParams);

		if (pathParams != null)
			webClient = webClient.resolveTemplates(pathParams);

		Builder builder = acceptMediaType(webClient.request());
		Response resp = builder.post(Entity.entity(object, getMediaType()));
		return getStatusResult(resp);
	}

	// UPDATE Methods

	@Override
	public T update(final WebTarget webClient, final T object, final String idName, final Long id)
			throws Exception {
		// TODO Auto-generated method stub
		return update(webClient, object, idName, id, null, null);
	}

	@Override
	public T update(WebTarget webClient, final T object, final String idName, final Long id,
			final Map<String, Object> pathParams, final Map<String, Object> queryParams)
			throws Exception {

		if (queryParams != null)
			webClient = applyQuery(webClient, queryParams);

		if (id != null && idName != null)
			webClient = webClient.resolveTemplate(idName, id);

		if (pathParams != null)
			webClient = webClient.resolveTemplates(pathParams);

		GenericType<T> genericType = new GenericType<T>(getClassP()) {
		};

		Builder builder = acceptMediaType(webClient.request());
		return builder.put(Entity.entity(object, getMediaType()), genericType);
	}

	@Override
	public StatusResult updateStatusReturn(final WebTarget webClient, final T object,
			final String idName, final Long id)
			throws Exception {
		return updateStatusReturn(webClient, object, idName, id, null, null);
	}

	@Override
	public StatusResult updateStatusReturn(WebTarget webClient, final T object,
			final String idName, final Long id,
			final Map<String, Object> pathParams, final Map<String, Object> queryParams)
			throws Exception {

		if (queryParams != null)
			webClient = applyQuery(webClient, queryParams);

		if (id != null && idName != null)
			webClient = webClient.resolveTemplate(idName, id);

		if (pathParams != null)
			webClient = webClient.resolveTemplates(pathParams);

		Builder builder = acceptMediaType(webClient.request());

		Response resp = builder.put(Entity.entity(object, getMediaType()));
		return getStatusResult(resp);

	}

	@Override
	public WebTarget applyQuery(WebTarget webClient, final Map<String, Object> queryParams)
			throws Exception {

		Iterator<String> it = queryParams.keySet().iterator();
		while (it.hasNext())
		{
			String key = it.next();
			webClient = webClient.queryParam(key, queryParams.get(key));
		}
		return webClient;
	}

	/* (non-Javadoc)
	 * @see org.appverse.web.framework.backend.rest.services.integration.IRestPersistenceService#mapPagedResult(javax.ws.rs.core.Response)
	 */
	@Override
	public IntegrationPaginatedResult<T> mapPagedResult(final Response response) throws Exception {
		throw new UnsupportedOperationException("You must overwrite 'mapPagedResult' method");
	}

	@Override
	public String getOffsetParamName() {
		return OFFSET_PARAM_NAME;
	}

	@Override
	public String getMaxRecordsParamName() {
		return MAX_RECORDS_PARAM_NAME;
	}

	@SuppressWarnings("unchecked")
	protected Class<T> getClassP() throws Exception {
		/*
				Class<T> classP = null;
				final Type type = this.getClass().getGenericSuperclass();
				if (type instanceof ParameterizedType) {
					final ParameterizedType pType = (ParameterizedType) type;
					if (pType.getActualTypeArguments()[0] instanceof Class) {
						classP = (Class<T>) pType.getActualTypeArguments()[0];
					} else {
						logger.error("", this.getClass());
						throw new Exception(this.getClass().getSimpleName());
					}
				} else {
					logger.error("", this.getClass());
					throw new Exception(this.getClass().getSimpleName());

				}

				return classP;
		*/
		Method method = this.getClass().getMethod("getTypeSafeList");

		Type returnType = method.getGenericReturnType();
		Class<T> classP = null;

		if (returnType instanceof ParameterizedType) {
			ParameterizedType type = (ParameterizedType) returnType;
			Type[] typeArguments = type.getActualTypeArguments();
			for (Type typeArgument : typeArguments) {
				classP = (Class<T>) typeArgument;
				System.out.println("typeArgClass = " + classP);
			}
		}
		return classP;

	}

	protected Type getTypeListP() throws Exception {
		return this.getClass().getMethod("getTypeSafeList").getGenericReturnType();
	}

	protected Invocation.Builder acceptMediaType(final Invocation.Builder builder)
	{
		return builder.accept(getMediaType());
	}

	@Override
	public String getMediaType()
	{
		return MediaType.APPLICATION_JSON;
	}

	/**
	 * Analyse response to get result code and message
	 * 
	 * @param resp
	 * @return
	 */
	protected StatusResult getStatusResult(final Response resp) throws Exception
	{
		StatusResult result = new StatusResult();
		result.setStatus(resp.getStatus());
		result.setLocation(resp.getLocation());
		try
		{
			String output = resp.readEntity(String.class);
			if (StringUtils.isNotBlank(output))
				result.setMessage(output);
			else
				result.setMessage(resp.getStatusInfo().getReasonPhrase());
		} catch (Exception e) {
		}
		return result;
	}

}
