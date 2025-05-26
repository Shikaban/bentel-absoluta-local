package cms.device.api;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import cms.device.util.Validators;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PluginValidator {
   private static PluginValidator instance;

   private PluginValidator() {
   }

   public static synchronized PluginValidator getDefault() {
      if (instance == null) {
         instance = new PluginValidator();
      }

      return instance;
   }

   public PluginValidator.Result validate(String var1, Object var2, String var3) {
      return Validators.getValidator(var3).validate(var1, var2);
   }

   public PluginValidator.Result validate(Map<String, Map<Object, String>> var1) {
      List<PluginValidator.Result> var2 = Lists.newArrayList();
      Iterator var3 = var1.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<String, Map<Object, String>> var4 = (Entry)var3.next();
         String var5 = (String)var4.getKey();
         Entry<Object, String> var6 = (Entry)((Map)var4.getValue()).entrySet().iterator().next();
         var2.add(this.validate(var5, var6.getKey(), (String)var6.getValue()));
      }

      return joinOperator(var2);
   }

   @VisibleForTesting
   static PluginValidator.Result andOperator(List<PluginValidator.Result> var0) {
      return (PluginValidator.Result)Iterables.find(var0, Predicates.not(PluginValidator.IsValid.INSTANCE), validResult());
   }

   @VisibleForTesting
   static PluginValidator.Result joinOperator(List<PluginValidator.Result> var0) {
      PluginValidator.Result var1 = andOperator(var0);
      if (var1.isValid()) {
         return var1;
      } else {
         PluginValidator.Result var2 = new PluginValidator.Result(false);
         Iterator var3 = var0.iterator();

         while(var3.hasNext()) {
            PluginValidator.Result var4 = (PluginValidator.Result)var3.next();
            if (!var4.isValid()) {
               var2.addReasons(var4.getReasons());
            }
         }

         return var2;
      }
   }

   public static PluginValidator.Result validResult() {
      return new PluginValidator.Result(true);
   }

   public static PluginValidator.Result invalidResult(String var0, String var1) {
      PluginValidator.Result var2 = new PluginValidator.Result(false);
      var2.addReasons(ImmutableList.of(String.format("%s.%s", var0, var1)));
      return var2;
   }

   private static enum IsValid implements Predicate<PluginValidator.Result> {
      INSTANCE;

      public boolean apply(PluginValidator.Result var1) {
         return var1.isValid();
      }
   }

   public static class Result {
      private final boolean isValid;
      private final List<String> reasons;

      Result(boolean var1) {
         this.isValid = var1;
         this.reasons = Lists.newArrayList();
      }

      public void addReasons(List<String> var1) {
         this.reasons.addAll(var1);
      }

      public boolean isValid() {
         return this.isValid;
      }

      public List<String> getReasons() {
         return ImmutableList.copyOf(this.reasons);
      }
   }
}
