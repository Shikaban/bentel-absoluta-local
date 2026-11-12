package protocol.dsc.commands;

import com.google.common.collect.ImmutableList;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import protocol.dsc.base.DscSerializable;

import java.util.List;

public abstract class DscRequestableCommand extends DscAbstractCommand implements DscResponse {
   protected abstract List<DscSerializable> getRequestFields();

   protected abstract List<DscSerializable> getOtherFields();

   protected final List<DscSerializable> getFields() {
      return ImmutableList.<DscSerializable>builder()
         .addAll(this.getRequestFields())
         .addAll(this.getOtherFields())
         .build();
   }

   public final void readFrom(ByteBuf inputBuffer) throws DecoderException, IndexOutOfBoundsException {
      super.readFrom(inputBuffer);
   }

   public final void readRequestDataFrom(ByteBuf inputBuffer) throws DecoderException, IndexOutOfBoundsException {
      List<DscSerializable> requestFields = this.getRequestFields();
      for (int i = 0; i < requestFields.size(); i++) {
         requestFields.get(i).readFrom(inputBuffer);
      }
   }

   public final void writeTo(ByteBuf buffer) {
      super.writeTo(buffer);
   }

   public final void writeRequestDataTo(ByteBuf buffer) {
      List<DscSerializable> requestFields = this.getRequestFields();
      for (int i = 0; i < requestFields.size(); i++) {
         requestFields.get(i).writeTo(buffer);
      }
   }

   public boolean match(DscRequestableCommand requestableCommand) {
      if (this.getClass() != requestableCommand.getClass()) {
         return false;
      } else {
         List<DscSerializable> requestFields = this.getRequestFields();
         List<DscSerializable> requestFields2 = requestableCommand.getRequestFields();
         int numericValue = requestFields.size();
         if (numericValue != requestFields2.size()) {
            return false;
         } else {
            for(int index = 0; index < numericValue; ++index) {
               if (!((DscSerializable)requestFields.get(index)).isEquivalent((DscSerializable)requestFields2.get(index))) {
                  return false;
               }
            }

            return true;
         }
      }
   }
}
