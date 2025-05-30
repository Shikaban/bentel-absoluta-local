package cms.device.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import cms.device.api.PluginValidator;

import java.util.Map;

public class Validators {
   private static final Map<String, ParamValidator> map = Maps.newHashMap();
   private static String CSV_INTEGERS_REGEX = "\\d+(,\\d+)*";
   private static final String UID_REGEX = "[0-9A-Fa-f]{8}";
   private static final String PIN_REGEX = "\\d{4,6}";

   private Validators() {
   }

   public static synchronized ParamValidator getValidator(String var0) {
      if (!map.containsKey(var0)) {
         map.put(var0, switchValidator(var0));
      }

      return (ParamValidator)map.get(var0);
   }

   private static ParamValidator switchValidator(String var0) {
      assert var0 != null;

      if (var0.equals("string")) {
         return new Validators.StringValidator();
      } else if (var0.equals("boolean")) {
         return new Validators.BooleanValidator();
      } else if (var0.equals("csvIntegers")) {
         return new Validators.RegexValidator(CSV_INTEGERS_REGEX, "invalid.csvIntegers");
      } else if (var0.equals("uid")) {
         return new Validators.RegexValidator(UID_REGEX, "invalid.uid");
      } else {
         return (ParamValidator)(var0.equals("pin") ? new Validators.RegexValidator(PIN_REGEX, "invalid.pin") : new Validators.NotNullValidator());
      }
   }

   @VisibleForTesting
   static String stringCast(String var0, Object var1) throws Validators.InvalidParamException {
      if (var1 instanceof String) {
         return (String)var1;
      } else {
         throw new Validators.InvalidParamException(PluginValidator.invalidResult(var0, "invalid.class"));
      }
   }

   private static class BooleanValidator extends Validators.SubValidator {
      public BooleanValidator() {
         super(new Validators.NotNullValidator());
      }

      public PluginValidator.Result doSubValidation(String var1, Object var2) throws Validators.InvalidParamException {
         PluginValidator.Result var3;
         if (var2 instanceof Boolean) {
            var3 = PluginValidator.validResult();
         } else {
            String var4 = Validators.stringCast(var1, var2);
            if (!"true".equalsIgnoreCase(var4) && !"false".equalsIgnoreCase(var4)) {
               var3 = PluginValidator.invalidResult(var1, "invalid.boolean");
            } else {
               var3 = PluginValidator.validResult();
            }
         }

         return var3;
      }
   }

   private abstract static class CatchingValidator implements ParamValidator {
      private CatchingValidator() {
      }

      protected abstract PluginValidator.Result doValidation(String var1, Object var2) throws Validators.InvalidParamException;

      public PluginValidator.Result validate(String var1, Object var2) {
         try {
            return this.doValidation(var1, var2);
         } catch (Validators.InvalidParamException var4) {
            return var4.result;
         }
      }

      // $FF: synthetic method
      CatchingValidator(Object var1) {
         this();
      }
   }

   private static class InvalidParamException extends IllegalArgumentException {
      private final PluginValidator.Result result;

      public InvalidParamException(PluginValidator.Result var1) {
         this.result = var1;
      }
   }

   private static class NotNullValidator implements ParamValidator {
      private NotNullValidator() {
      }

      public PluginValidator.Result validate(String var1, Object var2) {
         return var2 != null ? PluginValidator.validResult() : PluginValidator.invalidResult(var1, "null.value");
      }
   }

   private static class RegexValidator extends Validators.SubValidator {
      private final String pattern;
      private final String failReason;

      public RegexValidator(String var1, String var2) {
         super(new Validators.StringValidator());
         this.pattern = var1;
         this.failReason = var2;
      }

      public PluginValidator.Result doSubValidation(String var1, Object var2) throws Validators.InvalidParamException {
         return Validators.stringCast(var1, var2).trim().matches(this.pattern) ? PluginValidator.validResult() : PluginValidator.invalidResult(var1, this.failReason);
      }
   }

   private static class StringValidator extends Validators.SubValidator {
      public StringValidator() {
         super(new Validators.NotNullValidator());
      }

      public PluginValidator.Result doSubValidation(String var1, Object var2) throws Validators.InvalidParamException {
         String var3 = Validators.stringCast(var1, var2);
         return var3.trim().isEmpty() ? PluginValidator.invalidResult(var1, "empty.string") : PluginValidator.validResult();
      }
   }

   private abstract static class SubValidator extends Validators.CatchingValidator {
      private final ParamValidator superValidator;

      public SubValidator(ParamValidator var1) {
         super(null);
         this.superValidator = var1;
      }

      abstract PluginValidator.Result doSubValidation(String var1, Object var2) throws Validators.InvalidParamException;

      protected PluginValidator.Result doValidation(String var1, Object var2) throws Validators.InvalidParamException {
         PluginValidator.Result var3 = this.superValidator.validate(var1, var2);
         return var3.isValid() ? this.doSubValidation(var1, var2) : var3;
      }
   }

}
