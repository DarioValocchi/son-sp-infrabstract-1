/*
 * Copyright (c) 2015 SONATA-NFV, UCL, NOKIA, THALES, NCSR Demokritos ALL RIGHTS RESERVED.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Neither the name of the SONATA-NFV, UCL, NOKIA, NCSR Demokritos nor the names of its contributors
 * may be used to endorse or promote products derived from this software without specific prior
 * written permission.
 * 
 * This work has been performed in the framework of the SONATA project, funded by the European
 * Commission under Grant number 671517 through the Horizon 2020 and 5G-PPP programmes. The authors
 * would like to acknowledge the contributions of their colleagues of the SONATA partner consortium
 * (www.sonata-nfv.eu).
 *
 * @author Dario Valocchi (Ph.D.), UCL
 * 
 */

package sonata.kernel.WimAdaptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Observable;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import groovy.ui.OutputTransforms;
import sonata.kernel.WimAdaptor.commons.ComparableUuid;
import sonata.kernel.WimAdaptor.commons.ConfigureWanPayload;
import sonata.kernel.WimAdaptor.commons.DeployServiceResponse;
import sonata.kernel.WimAdaptor.commons.NapObject;
import sonata.kernel.WimAdaptor.commons.SonataManifestMapper;
import sonata.kernel.WimAdaptor.commons.Status;
import sonata.kernel.WimAdaptor.commons.VnfRecord;
import sonata.kernel.WimAdaptor.commons.vnfd.UnitDeserializer;
import sonata.kernel.WimAdaptor.messaging.ServicePlatformMessage;
import sonata.kernel.WimAdaptor.wrapper.WimWrapper;
import sonata.kernel.WimAdaptor.wrapper.WrapperBay;

public class ConfigureWimCallProcessor extends AbstractCallProcessor {

  private static final org.slf4j.Logger Logger =
      LoggerFactory.getLogger(ConfigureWimCallProcessor.class);

  public ConfigureWimCallProcessor(ServicePlatformMessage message, String sid, WimAdaptorMux mux) {
    super(message, sid, mux);
  }

  @Override
  public void update(Observable arg0, Object arg1) {
    // No async update mechanism for this call
  }

  @Override
  public boolean process(ServicePlatformMessage message) {

    ConfigureWanPayload request = null;
    boolean out = true;
    ObjectMapper mapper = SonataManifestMapper.getSonataMapper();
    try {
      request = mapper.readValue(message.getBody(), ConfigureWanPayload.class);
      Logger.info("payload parsed");
    } catch (Exception e) {
      Logger.error("Error parsing the wan configure payload: " + e.getMessage(), e);
      this.sendToMux(new ServicePlatformMessage(
          "{\"request_status\":\"fail\",\"message\":\"Payload parse error\"}", "application/json",
          message.getReplyTo(), message.getSid(), null));
      out = false;
    }

    String instanceId = request.getInstanceId();

    HashMap<String, ArrayList<String>> wim2VimsMap = new HashMap<String, ArrayList<String>>();
    ArrayList<ComparableUuid> vims = request.getVimList();
    Collections.sort(vims);
    ArrayList<String> vimsUuid = new ArrayList<String>(vims.size());
    for (ComparableUuid uuid : vims)
      vimsUuid.add(uuid.getUuid());
    for(String vimUuid : vimsUuid){
      WimWrapper wim = (WimWrapper) WrapperBay.getInstance().getWimRecordFromAttachedVim(vimUuid).getWimWrapper();
      if (wim2VimsMap.containsKey(wim.getConfig().getUuid())){
        wim2VimsMap.get(wim.getConfig().getUuid()).add(vimUuid);
      }else{
        ArrayList<String> vimsOfThisWim = new ArrayList<String>();
        vimsOfThisWim.add(vimUuid);
        wim2VimsMap.put(wim.getConfig().getUuid(), vimsOfThisWim);
      }
    }

    if(request.getNap()==null){
      for(String wimUuid: wim2VimsMap.keySet()){
        ArrayList<String> vimsOfThisWim = wim2VimsMap.get(wimUuid);
        Collections.sort(vimsOfThisWim);
        ArrayList<String> addressOfVims = new ArrayList<String>();
        for(String uuid : vimsOfThisWim){
          String address = WrapperBay.getInstance().getVimAddressFromVimUuid(uuid);
          if(address!=null)
            addressOfVims.add(address);
        }

        WimWrapper wim = (WimWrapper) WrapperBay.getInstance().getWimRecordFromWimUuid(wimUuid).getWimWrapper();
        String[] addressesArray = new String[addressOfVims.size()];
        addressesArray = addressOfVims.toArray(addressesArray);
        wim.configureNetwork(instanceId, null, null, addressesArray); 
      }
    }
      
    
    for(NapObject ingress_nap: request.getNap().getIngresses()){
      for(NapObject eggress_nap: request.getNap().getEgresses()){        
        for(String wimUuid: wim2VimsMap.keySet()){
          ArrayList<String> vimsOfThisWim = wim2VimsMap.get(wimUuid);
          Collections.sort(vimsOfThisWim);
          ArrayList<String> addressOfVims = new ArrayList<String>();
          for(String uuid : vimsOfThisWim){
            String address = WrapperBay.getInstance().getVimAddressFromVimUuid(uuid);
            if(address!=null)
              addressOfVims.add(address);
          }

          WimWrapper wim = (WimWrapper) WrapperBay.getInstance().getWimRecordFromWimUuid(wimUuid).getWimWrapper();
          String[] addressesArray = new String[addressOfVims.size()];
          addressesArray = addressOfVims.toArray(addressesArray);
          wim.configureNetwork(instanceId, ingress_nap.getNap(), eggress_nap.getNap(), addressesArray);
          
        }
      }
    }
    
    return out;
  }

  private void sendResponse(String message) {
    ServicePlatformMessage spMessage = new ServicePlatformMessage(message, "application/json",
        this.getMessage().getTopic(), this.getMessage().getSid(), null);
    this.sendToMux(spMessage);
  }
}
