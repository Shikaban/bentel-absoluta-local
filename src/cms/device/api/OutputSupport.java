package cms.device.api;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;

class OutputSupport {
   private final DeviceOrPanel deviceOrPanel;
   private final BiConsumer<String, Output.Action> controller;
   private Map<String, Output> outputs = Collections.emptyMap();

   OutputSupport(DeviceOrPanel var1, BiConsumer<String, Output.Action> var2) {
      this.deviceOrPanel = (DeviceOrPanel)Objects.requireNonNull(var1);
      this.controller = (BiConsumer)Objects.requireNonNull(var2);
   }

   void changeOutputs(List<String> var1) {
      if (!Iterables.elementsEqual(this.outputs.keySet(), var1)) {
         Map<String, Output> var2 = new LinkedHashMap(var1.size());
         var1.forEach((var2x) -> {
            if (this.outputs.containsKey(var2x)) {
               var2.put(var2x, this.outputs.get(var2x));
            } else {
               Output var3 = new Output(this.deviceOrPanel, var2x, (var2xx) -> {
                  this.controller.accept(var2x, var2xx);
               });
               var2.put(var2x, var3);
            }

         });
         this.outputs = Collections.unmodifiableMap(var2);
         this.deviceOrPanel.fireChange();
      }

   }

   Map<String, Output> getOutputs() {
      return this.outputs;
   }

   Output getOutput(String var1) {
      Output var2 = (Output)this.outputs.get(var1);
      if (var2 != null) {
         return var2;
      } else {
         throw new NoSuchElementException(String.format("output '%s'", var1));
      }
   }

   void setOutputRemoteName(String var1, String var2) {
      this.getOutput(var1).setRemoteName(var2);
   }

   void setOutputEnabled(String var1, boolean var2) {
      this.getOutput(var1).setEnabled(var2);
   }

   void setOutputType(String var1, Output.Type var2) {
      this.getOutput(var1).setType(var2);
   }

   void setOutputStatus(String var1, Output.Status var2) {
      this.getOutput(var1).setStatus(var2);
   }

   public void fireChange() {
      this.outputs.values().forEach((var0) -> {
         var0.fireChange();
      });
   }
}
