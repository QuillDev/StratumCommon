package moe.quill.stratumcommon.Services;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import io.papermc.paper.text.PaperComponents;
import moe.quill.StratumCommon.Serialization.ISerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.commons.lang.SerializationUtils;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class StratumSerialization implements ISerializer {
    private static final Kryo kryo = new Kryo();
    private static GsonComponentSerializer componentSerializer;

    public StratumSerialization() {
        kryo.setRegistrationRequired(false);
        kryo.addDefaultSerializer(Float.class, DefaultSerializers.FloatSerializer.class);
        kryo.addDefaultSerializer(String.class, DefaultSerializers.StringSerializer.class);
        kryo.addDefaultSerializer(Boolean.class, DefaultSerializers.BooleanSerializer.class);
        kryo.addDefaultSerializer(Long.class, DefaultSerializers.LongSerializer.class);
        componentSerializer = PaperComponents.gsonSerializer();
    }

    /**
     * Serialize a list of items to a byte array
     *
     * @param itemStacks to serialize
     * @return the serialized item list
     */
    public byte[] serializeItemList(ArrayList<ItemStack> itemStacks) {
        final var arrayOfItemByteArrays = itemStacks
                .stream()
                .map(ItemStack::serializeAsBytes)
                .collect(Collectors.toCollection(ArrayList::new));
        return SerializationUtils.serialize(arrayOfItemByteArrays);
    }

    /**
     * Deserialize the item list
     *
     * @param bytes to deserialize
     * @return the array list we got from the bytes
     */
    public ArrayList<ItemStack> deserializeItemList(byte[] bytes) {
        final var byteList = (ArrayList<byte[]>) SerializationUtils.deserialize(bytes);
        if (byteList == null) return null;
        return byteList
                .stream()
                .map(ItemStack::deserializeBytes)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public byte[] serializeLong(long value) {
        final var output = new Output(8);
        kryo.writeObject(output, value);
        return output.getBuffer();
    }

    public Long deserializeLong(byte[] bytes) {
        final var input = new Input(bytes);
        return kryo.readObject(input, Long.class);
    }

    public byte[] serializeComponentList(ArrayList<Component> itemStacks) {
        final var arrayOfComponentBytes = itemStacks
                .stream()
                .map(this::serializeComponent)
                .collect(Collectors.toCollection(ArrayList::new));
        return SerializationUtils.serialize(arrayOfComponentBytes);
    }

    /**
     * Deserialize the item list
     *
     * @param bytes to deserialize
     * @return the array list we got from the bytes
     */
    public ArrayList<Component> deserializeComponentList(byte[] bytes) {
        final var byteList = (ArrayList<byte[]>) SerializationUtils.deserialize(bytes);
        if (byteList == null) return null;
        return byteList
                .stream()
                .map(this::deserializeComponent)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public byte[] serializeComponent(Component component) {
        final var gsonString = componentSerializer.serialize(component);
        return serializeString(gsonString);
    }

    public Component deserializeComponent(byte[] bytes) {
        final var gsonString = deserializeString(bytes);
        return componentSerializer.deserialize(gsonString);
    }

    /**
     * Serialize the boolean into bytes
     *
     * @param value to serialize into bytes
     * @return the boolean constructed from the bytes
     */
    public byte[] serializeBoolean(Boolean value) {
        final var output = new Output(1);
        kryo.writeObject(output, value);
        return output.getBuffer();
    }

    /**
     * Deserialize the item from bytes into a boolean
     *
     * @param bytes to make a boolean from
     * @return a boolean from the bytes
     */
    public Boolean deserializeBoolean(byte[] bytes) {
        final var input = new Input(bytes);
        return kryo.readObject(input, Boolean.class);
    }

    /**
     * Serialize the item stack into bytes
     *
     * @param itemStack to turn serialize into bytes
     * @return the bytes from the item stack
     */
    public byte[] serializeItemStack(ItemStack itemStack) {
        return itemStack.serializeAsBytes();
    }

    /**
     * Deserialize bytes into an item stack
     *
     * @param bytes to deserialize the item from
     * @return the item stack from the bytes
     */
    public ItemStack deserializeItemStack(byte[] bytes) {
        return ItemStack.deserializeBytes(bytes);
    }

    /**
     * Serialize the string into raw bytes
     *
     * @param value to convert to bytes
     * @return the string from the raw bytes
     */
    public byte[] serializeString(String value) {
        final var output = new Output(2048);
        kryo.writeObject(output, value);
        return output.getBuffer();
    }

    /**
     * Deserialize the string from the byte values
     *
     * @param bytes to convert to string from bytes
     * @return the string from the raw bytes
     */
    public String deserializeString(byte[] bytes) {
        final var input = new Input(bytes);
        return kryo.readObject(input, String.class);
    }

    /**
     * Serialize and deserialize floats
     *
     * @param value of the float to encode
     * @return the bytes for that float
     */
    public byte[] serializeFloat(float value) {
        final var output = new Output(4);
        kryo.writeObject(output, value);
        return output.getBuffer();
    }

    /**
     * Deserialize the float from the byte values
     *
     * @param bytes to convert to float objects
     * @return the float from the given raw bytes
     */
    public float deserializeFloat(byte[] bytes) {
        final var input = new Input(bytes);
        return kryo.readObject(input, Float.class);
    }
}
