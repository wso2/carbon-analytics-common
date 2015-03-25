<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License"); you may not
  ~ use this file except in compliance with the License. You may obtain a copy
  ~ of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software distributed
  ~ under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  ~ CONDITIONS OF ANY KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations under the License.
  --%>
<%@ page
	import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub"%>
<%@ page
	import="org.wso2.carbon.event.stream.ui.EventStreamUIUtils"%>
<%@ page
	import="org.wso2.carbon.event.stream.stub.types.EventStreamAttributeDto"%>
<%@ page
	import="org.wso2.carbon.event.stream.stub.types.EventStreamDefinitionDto"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<fmt:bundle
	basename="org.wso2.carbon.event.stream.ui.i18n.Resources">

	<carbon:breadcrumb label="eventstream.detail"
		resourceBundle="org.wso2.carbon.event.stream.ui.i18n.Resources"
		topPage="false" request="<%=request%>" />


	<link type="text/css" href="css/eventStream.css" rel="stylesheet" />
	<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
	<script type="text/javascript" src="../admin/js/cookies.js"></script>
	<script type="text/javascript" src="../admin/js/main.js"></script>
	<script type="text/javascript"
		src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
	<script type="text/javascript"
		src="../yui/build/connection/connection-min.js"></script>
	<script type="text/javascript" src="../eventstream/js/event_stream.js"></script>
	<script type="text/javascript"
		src="../eventstream/js/create_eventStream_helper.js"></script>
	<script type="text/javascript" src="../ajax/js/prototype.js"></script>
	<script type="text/javascript"
		src="../eventstream/js/vkbeautify.0.99.00.beta.js"></script>

	<%
		String eventStreamWithVersion = request.getParameter("eventStreamWithVersion");
	    	    		EventStreamAdminServiceStub eventStreamAdminServiceStub = EventStreamUIUtils
	    				.getEventStreamAdminService(config, session, request);
	    		EventStreamDefinitionDto streamDefinitionDto = eventStreamAdminServiceStub
	    				.getStreamDefinitionDto(eventStreamWithVersion);
	%>

	<script type="text/javascript">
		jQuery(document).ready(function() {
			formatSampleEvent();
		});

		function formatSampleEvent() {

			var selectedIndex = document
					.getElementById("sampleEventTypeFilter").selectedIndex;
			var eventType = document.getElementById("sampleEventTypeFilter").options[selectedIndex].text;

			var sampleEvent = document.getElementById("sampleEventText").value
					.trim();

			if (eventType == "xml") {
				jQuery('#sampleEventText').val(
						vkbeautify.xml(sampleEvent.trim()));
			} else if (eventType == "json") {
				jQuery('#sampleEventText').val(
						vkbeautify.json(sampleEvent.trim()));
			}
		}
	</script>
	<script type="text/javascript">
		jQuery(document).ready(function() {
			changeView('graphics');
		});
		function changeView(view) {
			var plain = "source";
			if (plain.localeCompare(view) == 0) {
				document.getElementById("designInnerDiv").style.display = "none";
				document.getElementById("sourceInnerDiv").style.display = "inline";
			} else {
				document.getElementById("sourceInnerDiv").style.display = "none";
				document.getElementById("designInnerDiv").style.display = "inline";
			}
		}
	</script>

	<div id="middle">
		<h2>
			<fmt:message key="event.stream.details" /><%=eventStreamWithVersion%>
		</h2>

		<div id="workArea">
			<form name="eventStreamInfo" action="index.jsp?ordinal=1"
				method="post" id="showEventStream">
				<table id="eventStreamInfoTable" class="styledLeft"
					style="width: 100%">

					<thead>
						<tr>
							<th><fmt:message key="event.stream.details" /></th>
						</tr>
					</thead>
					<tbody>

						<tr>
							<td class="formRaw">
								<table id="eventStreamDetailTable1" class="normal-nopadding"
									style="width: 100%">

									<tbody>
										<%
											if (eventStreamWithVersion != null) {
													EventStreamAdminServiceStub stub =
													                                   EventStreamUIUtils.getEventStreamAdminService(config,
													                                                                                 session,
													                                                                                 request);
													String[] eventAdaptorPropertiesDto =
													                                     stub.getStreamDetailsForStreamId(eventStreamWithVersion);
										%>
										<tr>
											<td colspan="2">
												<div id="designInnerDiv">
													<table class="styledLeft noBorders spacer-bot"
														style="width: 100%">
														<tbody>
															<tr name="eventDetails">
																<td colspan="2" class="middle-header"><span
																	style="float: left; position: relative; margin-top: 2px;"><fmt:message
																			key="event.stream.definition" /> </span> <a href="#"
																	onclick="changeView('source');" class="icon-link"
																	style="background-image: url(images/source-view.gif); font-weight: normal">
																		switch to source view </a></td>

															</tr>
															<tr name="eventDetails">
																<td>
																	<h6>
																		<fmt:message key="event.stream.name" />
																	</h6>
																</td>
																<td style="padding-top: 10px"><input type="text"
																	name="eventStreamName" id="eventStreamNameId"
																	class="initE"
																	value="<%=streamDefinitionDto.getName()%>"
																	style="width: 75%;" readonly="true" />

																	<div class="sectionHelp">
																		<fmt:message key="event.stream.name.help" />
																	</div></td>
															</tr>
															<tr name="eventDetails">
																<td>
																	<h6>
																		<fmt:message key="event.stream.version" />
																	</h6>
																</td>
																<td style="padding-top: 10px"><input type="text"
																	name="eventStreamVersion" id="eventStreamVersionId"
																	class="initE"
																	value="<%=streamDefinitionDto.getVersion()%>"
																	style="width: 75%" readonly="true" />

																	<div class="sectionHelp">
																		<fmt:message key="event.stream.version.help" />
																	</div></td>
															</tr>

															<tr>
																<td>
																	<h6>
																		<fmt:message key="event.stream.description" />
																	</h6>
																</td>
																<td style="padding-top: 10px"><input type="text"
																	name="eventStreamDescription"
																	id="eventStreamDescription" class="initE"
																	value="<%=streamDefinitionDto.getDescription() != null
					                                                      ? streamDefinitionDto.getDescription()
					                                                      : ""%>"
																	style="width: 75%" readonly="true" />
																	<div class="sectionHelp">
																		<fmt:message key="event.stream.description.help" />
																	</div></td>
															</tr>

															<tr>
																<td>
																	<h6>
																		<fmt:message key="event.stream.nickname" />
																	</h6>
																</td>
																<td style="padding-top: 10px"><input type="text"
																	name="eventStreamNickName" id="eventStreamNickName"
																	class="initE"
																	value="<%=streamDefinitionDto.getNickName() != null
					                                                   ? streamDefinitionDto.getNickName()
					                                                   : ""%>"
																	style="width: 75%" readonly="true" />

																	<div class="sectionHelp">
																		<fmt:message key="event.stream.nickname.help" />
																	</div></td>
															</tr>

															<tr name="eventDetails">
																<!-- <td colspan="2"  class="middle-header"> -->
																<td colspan="2" class="middle-header"><h6>
																		<fmt:message key="stream.attributes" />
																	</h6></td>
															</tr>
															<tr name="streamAttributes">
																<td colspan="2">
																	<h6>
																		<fmt:message key="attribute.data.type.meta" />
																	</h6> <%
 	if (streamDefinitionDto.getMetaData() != null &&
 			    streamDefinitionDto.getMetaData().length > 0) {
 %>
																	<table class="styledLeft noBorders spacer-bot"
																		id="outputMetaDataTable">
																		<thead>
																			<th class="leftCol-med"><fmt:message
																					key="attribute.name" /></th>
																			<th class="leftCol-med"><fmt:message
																					key="attribute.type" /></th>

																		</thead>
																		<%
																			for (EventStreamAttributeDto metaData : streamDefinitionDto.getMetaData()) {
																		%>
																		<tr>
																			<td class="property-names"><%=metaData.getAttributeName()%>
																			</td>
																			<td class="property-names"><%=metaData.getAttributeType()%>
																			</td>
																		</tr>
																		<%
																			}
																		%>
																	</table> <%
 	} else {
 %>
																	<table class="styledLeft noBorders spacer-bot"
																		id="outputMetaDataTable" style="display: none">
																		<thead>
																			<th class="leftCol-med"><fmt:message
																					key="attribute.name" /></th>
																			<th class="leftCol-med"><fmt:message
																					key="attribute.type" /></th>
																		</thead>
																	</table>
																	<div class="noDataDiv-plain" id="noOutputMetaData">
																		<fmt:message key="no.meta.attributes.defined" />
																	</div> <%
 	}
 %>
																</td>
															</tr>
															<tr name="streamAttributes">
																<td colspan="2">
																	<h6>
																		<fmt:message key="attribute.data.type.correlation" />
																	</h6> <%
 	if (streamDefinitionDto.getCorrelationData() != null &&
 			    streamDefinitionDto.getCorrelationData().length > 0) {
 %>
																	<table class="styledLeft noBorders spacer-bot"
																		id="outputCorrelationDataTable">
																		<thead>
																			<th class="leftCol-med"><fmt:message
																					key="attribute.name" /></th>
																			<th class="leftCol-med"><fmt:message
																					key="attribute.type" /></th>
																		</thead>
																		<%
																			for (EventStreamAttributeDto correlationData : streamDefinitionDto.getCorrelationData()) {
																		%>
																		<tr>
																			<td class="property-names"><%=correlationData.getAttributeName()%>
																			</td>
																			<td class="promacbook Mini DisplayPort to VGA Adapterperty-names"><%=correlationData.getAttributeType()%>
																			</td>
																		</tr>
																		<%
																			}
																		%>
																	</table> <%
 	} else {
 %>
																	<table class="styledLeft noBorders spacer-bot"
																		id="outputCorrelationDataTable" style="display: none">
																		<thead>
																			<th class="leftCol-med"><fmt:message
																					key="attribute.name" /></th>
																			<th class="leftCol-med"><fmt:message
																					key="attribute.type" /></th>
																			<th><fmt:message key="actions" /></th>
																		</thead>
																	</table>
																	<div class="noDataDiv-plain"
																		id="noOutputCorrelationData">
																		<fmt:message key="no.correlation.attributes.defined" />
																	</div> <%
 	}
 %>
																</td>
															</tr>
															<tr name="streamAttributes">
																<td colspan="2">
																	<h6>
																		<fmt:message key="attribute.data.type.payload" />
																	</h6> <%
 	if (streamDefinitionDto.getPayloadData() != null &&
 			    streamDefinitionDto.getPayloadData().length > 0) {
 %>
																	<table class="styledLeft noBorders spacer-bot"
																		id="outputPayloadDataTable">
																		<thead>
																			<th class="leftCol-med"><fmt:message
																					key="attribute.name" /></th>
																			<th class="leftCol-med"><fmt:message
																					key="attribute.type" /></th>
																		</thead>
																		<%
																			for (EventStreamAttributeDto payloadData : streamDefinitionDto.getPayloadData()) {
																		%>
																		<tr>
																			<td class="property-names"><%=payloadData.getAttributeName()%>
																			</td>
																			<td class="property-names"><%=payloadData.getAttributeType()%>
																			</td>
																		</tr>
																		<%
																			}
																		%>
																	</table> <%
 	}
 %>
																</td>
															</tr>
														</tbody>
													</table>
												</div>
												<div id="sourceInnerDiv">

													<table id="eventStreamDetailTable2"
														class="styledLeft noBorders spacer-bot"
														style="width: 100%">
														<tbody>
															<tr name="eventDetails">
																<td colspan="2" class="middle-header"><span
																	style="float: left; position: relative; margin-top: 2px;"><fmt:message
																			key="event.stream.definition" /></span> <a href="#"
																	onclick="changeView('design');" class="icon-link"
																	style="background-image: url(images/design-view.gif); font-weight: normal">
																		switch to design view </a></td>

															</tr>
															<tr>
																<td colspan="2"><textArea class="expandedTextarea"
																		id="streamDefinitionText" name="streamDefinitionText"
																		readonly="true" cols="120" style="height: 350px;"><%=eventAdaptorPropertiesDto[0]%>
					                                    		</textArea></td>
															</tr>
														</tbody>
													</table>
												</div>
											</td>
										</tr>
										<tr>
											<td colspan="2">
												<div id="sampleEventGenerater">
													<table class="styledLeft noBorders spacer-bot"
														style="width: 100%">
														<tbody>
															<tr name="createSampleEventType">
																<td colspan="2" class="middle-header"><fmt:message
																		key="generate.event.title" /></td>
															</tr>
															<tr>
																<td colspan="2"><select
																	name="sampleEventTypeFilter" id="sampleEventTypeFilter">
																		<option>xml</option>
																		<option>json</option>
																		<option>text</option>
																</select> <input type="button"
																	value="<fmt:message key="generate.event"/>"
																	onclick="generateEvent('<%=eventStreamWithVersion%>')" />
																</td>
															</tr>
															<tr>
																<td colspan="2"><textArea class="expandedTextarea"
																		id="sampleEventText" name="sampleEventText"
																		readonly="true" cols="120"><%=eventAdaptorPropertiesDto[1]%>
							                                    </textArea></td>
															</tr>
														</tbody>
													</table>
												</div>
											</td>

										</tr>

										<%
											}
										%>
									</tbody>
								</table>
							</td>
						</tr>
					</tbody>

				</table>

			</form>
		</div>
	</div>
</fmt:bundle>
