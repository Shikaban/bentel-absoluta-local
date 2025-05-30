package cms.device.spi;

import com.google.common.collect.ImmutableList;
import cms.device.api.Connectable;
import cms.device.api.Device;
import cms.device.api.Input;
import cms.device.api.NotifierEventSource;
import cms.device.api.Output;
import cms.device.api.Panel;
import cms.device.api.Partition;
import java.util.List;

public enum SourceType {
   INPUT(Input.class) {
      public List<Input> getSources(Connectable var1) {
         Panel var2 = (Panel)var1;
         return (List)var2.getInputs().values();
      }

      public NotifierEventSource getSource(Connectable var1, String var2) {
         Panel var3 = (Panel)var1;
         return (NotifierEventSource)var3.getInputs().get(var2);
      }
   },
   PARTITION(Partition.class) {
      public List<Partition> getSources(Connectable var1) {
         Panel var2 = (Panel)var1;
         return (List)var2.getPartitions().values();
      }

      public NotifierEventSource getSource(Connectable var1, String var2) {
         Panel var3 = (Panel)var1;
         return (NotifierEventSource)var3.getPartitions().get(var2);
      }
   },
   OUTPUT(Output.class) {
      public List<Output> getSources(Connectable var1) {
         if (var1 instanceof Device) {
            Device var3 = (Device)var1;
            return var3.getOutputs();
         } else if (var1 instanceof Panel) {
            Panel var2 = (Panel)var1;
            return ImmutableList.copyOf(var2.getOutputs().values());
         } else {
            throw new IllegalArgumentException("connectable '" + var1 + "' has no outputs");
         }
      }

      public Output getSource(Connectable var1, String var2) {
         if (var1 instanceof Device) {
            Device var4 = (Device)var1;
            return var4.getOutput(var2);
         } else if (var1 instanceof Panel) {
            Panel var3 = (Panel)var1;
            return (Output)var3.getOutputs().get(var2);
         } else {
            throw new IllegalArgumentException("connectable '" + var1 + "' has no outputs");
         }
      }
   };

   private final Class<? extends NotifierEventSource> relatedClass;

   private SourceType(Class<? extends NotifierEventSource> var3) {
      this.relatedClass = var3;
   }

   public Class<? extends NotifierEventSource> getRelatedClass() {
      return this.relatedClass;
   }

   public abstract List<? extends NotifierEventSource> getSources(Connectable var1);

   public NotifierEventSource getSource(Connectable var1, int var2) {
      return (NotifierEventSource)this.getSources(var1).get(var2);
   }

   public abstract NotifierEventSource getSource(Connectable var1, String var2);

   public static SourceType fromSourceClass(Class<? extends NotifierEventSource> var0) {
      SourceType[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         SourceType var4 = var1[var3];
         if (var4.getRelatedClass().isAssignableFrom(var0)) {
            return var4;
         }
      }

      throw new IllegalArgumentException(var0.getName());
   }

   // $FF: synthetic method
   SourceType(Class var3, Object var4) {
      this(var3);
   }
}
