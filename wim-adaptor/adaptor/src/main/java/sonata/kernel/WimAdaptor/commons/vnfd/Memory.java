/**
 * @author Dario Valocchi (Ph.D.)
 * @mail d.valocchi@ucl.ac.uk
 * 
 *       Copyright 2016 [Dario Valocchi]
 * 
 *       Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 *       except in compliance with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *       Unless required by applicable law or agreed to in writing, software distributed under the
 *       License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *       either express or implied. See the License for the specific language governing permissions
 *       and limitations under the License.
 * 
 */

package sonata.kernel.WimAdaptor.commons.vnfd;

import com.fasterxml.jackson.annotation.JsonProperty;

import sonata.kernel.WimAdaptor.commons.vnfd.Unit.MemoryUnit;

public class Memory {


  private double size;
  @JsonProperty("size_unit")
  private MemoryUnit sizeUnit;
  @JsonProperty("large_pages_required")
  private boolean largePagesRequired;
  @JsonProperty("numa_allocation_policy")
  private String numaAllocationPolicy;



  public void setSize(double size) {
    this.size = size;
  }

  public void setSizeUnit(MemoryUnit sizeUnit) {
    this.sizeUnit = sizeUnit;
  }

  public void setLargePagesRequired(boolean largePagesRequired) {
    this.largePagesRequired = largePagesRequired;
  }

  public void setNumaAllocationPolicy(String numaAllocationPolicy) {
    this.numaAllocationPolicy = numaAllocationPolicy;
  }

  public double getSize() {
    return size;
  }

  public MemoryUnit getSizeUnit() {
    return sizeUnit;
  }

  public boolean isLargePagesRequired() {
    return largePagesRequired;
  }

  public String getNumaAllocationPolicy() {
    return numaAllocationPolicy;
  }

}
