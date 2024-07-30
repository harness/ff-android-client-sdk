package io.harness.cfsdk.wrapperexample

import android.content.Context
import io.harness.cfsdk.CfClient
import io.harness.cfsdk.CfConfiguration
import io.harness.cfsdk.cloud.model.Target
import io.harness.cfsdk.cloud.sse.EventsListener
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Example wrapper class for the Harness FF Android SDK.
 * Provides convenience methods for managing feature flags and evaluating their values.
 * You may need to tweak this class depending on your needs.
 *
 * @property client The instance of the Harness FF SDK client.
 * @property flagIds The map of flag IDs to their data.
 */
class HarnessFfSdkWrapper(

) {

    private val client: CfClient = CfClient.getInstance()
    private val flagIds: ConcurrentHashMap<String, FlagData> = ConcurrentHashMap()
    private val isInitialized = AtomicBoolean(false)

    /**
     * Initializes the Harness FF SDK.
     *
     * @param context The application context.
     * @param apiKey The API key for initializing the Harness FF SDK.
     * @param config The configuration for the Harness FF SDK.
     * @param target The target for the Harness FF SDK.
     * @param callback Callback to handle the result of the initialization.
     */
    fun initialize(
        context: Context,
        apiKey: String,
        config: CfConfiguration,
        target: Target,
        callback: (Boolean, String?) -> Unit
    ) {
        client.initialize(
            context, apiKey, config, target
        ) { _, result ->
            if (result.isSuccess) {
                isInitialized.set(true)
                callback(true, null)
            } else {
                callback(false, result.error.toString())
            }
        }
    }

    /**
     * Data class representing the flag data.
     *
     * @property type The type of the flag.
     * @property defaultValue The default value of the flag.
     */
    data class FlagData(val type: FlagType, val defaultValue: DefaultValue)

    /**
     * Sealed class representing the possible flag values.
     */
    sealed class FlagValue {
        data class BooleanFlag(val value: Boolean) : FlagValue()
        data class StringFlag(val value: String) : FlagValue()
        data class NumberFlag(val value: Double) : FlagValue()
        data class JsonFlag(val value: JSONObject) : FlagValue()
    }

    /**
     * Sealed class representing the possible default values.
     */
    sealed class DefaultValue {
        data class BooleanDefault(val value: Boolean) : DefaultValue()
        data class StringDefault(val value: String) : DefaultValue()
        data class NumberDefault(val value: Double) : DefaultValue()
        data class JsonDefault(val value: JSONObject) : DefaultValue()
    }

    /**
     * Enum class representing the types of flags.
     */
    enum class FlagType {
        BOOLEAN, STRING, NUMBER, JSON
    }

    /**
     * Exception thrown when a flag ID is not found.
     *
     * @property id The flag ID.
     */
    class FlagNotFoundException(id: String) : Exception("Flag ID '$id' not found in the list.")

    /**
     * Exception thrown when the flag type is invalid.
     *
     * @property id The flag ID.
     * @property expectedType The expected flag type.
     * @property actualType The actual flag type.
     */
    class InvalidFlagTypeException(id: String, expectedType: FlagType, actualType: FlagType) :
        Exception("Flag ID '$id' is of type '$actualType' but expected type was '$expectedType'.")

    /**
     * Adds a flag to the wrapper.
     *
     * @param id The flag ID.
     * @param type The flag type.
     * @param defaultValue The default value of the flag.
     * @throws IllegalArgumentException If the default value type does not match the flag type.
     */
    @Throws(IllegalArgumentException::class)
    fun addFlag(id: String, type: FlagType, defaultValue: DefaultValue) {
        validateDefaultValue(type, defaultValue)
        flagIds[id] = FlagData(type, defaultValue)
    }

    /**
     * Removes a flag from the wrapper.
     *
     * @param id The flag ID.
     * @throws FlagNotFoundException If the flag ID is not found in the list.
     */
    @Throws(FlagNotFoundException::class)
    fun removeFlag(id: String) {
        flagIds.remove(id) ?: throw FlagNotFoundException(id)
    }

    /**
     * Gets the type of a flag.
     *
     * @param id The flag ID.
     * @return The flag type.
     * @throws FlagNotFoundException If the flag ID is not found in the list.
     */
    @Throws(FlagNotFoundException::class)
    fun getFlagType(id: String): FlagType {
        return flagIds[id]?.type ?: throw FlagNotFoundException(id)
    }

    /**
     * Gets the default value of a flag.
     *
     * @param id The flag ID.
     * @return The default value of the flag.
     * @throws FlagNotFoundException If the flag ID is not found in the list.
     */
    @Throws(FlagNotFoundException::class)
    fun getFlagDefaultValue(id: String): DefaultValue {
        return flagIds[id]?.defaultValue ?: throw FlagNotFoundException(id)
    }

    /**
     * Evaluates a flag.
     *
     * @param id The flag ID.
     * @return The evaluated flag value.
     * @throws FlagNotFoundException If the flag ID is not found in the list.
     */
    @Throws(FlagNotFoundException::class)
    fun evaluateFlag(id: String): FlagValue {
        val flagData = flagIds[id] ?: throw FlagNotFoundException(id)
        return when (flagData.type) {
            FlagType.BOOLEAN -> FlagValue.BooleanFlag(
                client.boolVariation(
                    id,
                    (flagData.defaultValue as DefaultValue.BooleanDefault).value
                )
            )

            FlagType.STRING -> FlagValue.StringFlag(
                client.stringVariation(
                    id,
                    (flagData.defaultValue as DefaultValue.StringDefault).value
                )
            )

            FlagType.NUMBER -> FlagValue.NumberFlag(
                client.numberVariation(
                    id,
                    (flagData.defaultValue as DefaultValue.NumberDefault).value
                )
            )

            FlagType.JSON -> FlagValue.JsonFlag(
                client.jsonVariation(
                    id,
                    (flagData.defaultValue as DefaultValue.JsonDefault).value
                )
            )
        }
    }

    /**
     * Evaluates a boolean flag.
     *
     * @param id The flag ID.
     * @return The evaluated boolean flag value.
     * @throws FlagNotFoundException If the flag ID is not found in the list.
     * @throws InvalidFlagTypeException If the flag type is not BOOLEAN.
     */
    @Throws(FlagNotFoundException::class, InvalidFlagTypeException::class)
    fun evaluateBooleanFlag(id: String): Boolean {
        val flagData = flagIds[id] ?: throw FlagNotFoundException(id)
        if (flagData.type != FlagType.BOOLEAN) throw InvalidFlagTypeException(
            id,
            FlagType.BOOLEAN,
            flagData.type
        )
        return client.boolVariation(
            id,
            (flagData.defaultValue as DefaultValue.BooleanDefault).value
        )
    }

    /**
     * Evaluates a string flag.
     *
     * @param id The flag ID.
     * @return The evaluated string flag value.
     * @throws FlagNotFoundException If the flag ID is not found in the list.
     * @throws InvalidFlagTypeException If the flag type is not STRING.
     */
    @Throws(FlagNotFoundException::class, InvalidFlagTypeException::class)
    fun evaluateStringFlag(id: String): String {
        val flagData = flagIds[id] ?: throw FlagNotFoundException(id)
        if (flagData.type != FlagType.STRING) throw InvalidFlagTypeException(
            id,
            FlagType.STRING,
            flagData.type
        )
        return client.stringVariation(
            id,
            (flagData.defaultValue as DefaultValue.StringDefault).value
        )
    }

    /**
     * Evaluates a number flag.
     *
     * @param id The flag ID.
     * @return The evaluated number flag value.
     * @throws FlagNotFoundException If the flag ID is not found in the list.
     * @throws InvalidFlagTypeException If the flag type is not NUMBER.
     */
    @Throws(FlagNotFoundException::class, InvalidFlagTypeException::class)
    fun evaluateNumberFlag(id: String): Double {
        val flagData = flagIds[id] ?: throw FlagNotFoundException(id)
        if (flagData.type != FlagType.NUMBER) throw InvalidFlagTypeException(
            id,
            FlagType.NUMBER,
            flagData.type
        )
        return client.numberVariation(
            id,
            (flagData.defaultValue as DefaultValue.NumberDefault).value
        )
    }

    /**
     * Evaluates a JSON flag.
     *
     * @param id The flag ID.
     * @return The evaluated JSON flag value.
     * @throws FlagNotFoundException If the flag ID is not found in the list.
     * @throws InvalidFlagTypeException If the flag type is not JSON.
     */
    @Throws(FlagNotFoundException::class, InvalidFlagTypeException::class)
    fun evaluateJsonFlag(id: String): JSONObject {
        val flagData = flagIds[id] ?: throw FlagNotFoundException(id)
        if (flagData.type != FlagType.JSON) throw InvalidFlagTypeException(
            id,
            FlagType.JSON,
            flagData.type
        )
        return client.jsonVariation(id, (flagData.defaultValue as DefaultValue.JsonDefault).value)
    }

    /**
     * Checks if all flags are enabled.
     *
     * @return `true` if all flags are enabled, `false` otherwise.
     */
    fun areAllFlagsEnabled(): Boolean {
        for ((id, flagData) in flagIds) {
            when (flagData.type) {
                FlagType.BOOLEAN -> if (!client.boolVariation(
                        id,
                        (flagData.defaultValue as DefaultValue.BooleanDefault).value
                    )
                ) return false

                FlagType.STRING -> if (client.stringVariation(
                        id,
                        (flagData.defaultValue as DefaultValue.StringDefault).value
                    ) == ""
                ) return false

                FlagType.NUMBER -> if (client.numberVariation(
                        id,
                        (flagData.defaultValue as DefaultValue.NumberDefault).value
                    ) == 0.0
                ) return false

                FlagType.JSON -> if (client.jsonVariation(
                        id,
                        (flagData.defaultValue as DefaultValue.JsonDefault).value
                    ).length() == 0
                ) return false
            }
        }
        return true
    }

    /**
     * Adds an event listener to the Harness FF SDK.
     *
     * @param listener The event listener.
     */
    fun addEventListener(listener: EventsListener) {
        client.registerEventsListener(listener)
    }

    /**
     * Removes an event listener from the Harness FF SDK.
     *
     * @param listener The event listener.
     */
    fun removeEventListener(listener: EventsListener) {
        client.unregisterEventsListener(listener)
    }

    /**
     * Checks if the SDK is initialized.
     *
     * @return `true` if the SDK is initialized, `false` otherwise.
     */
    fun isInitialized(): Boolean {
        return isInitialized.get()
    }


    /**
     * Validates the default value type against the flag type.
     *
     * @param type The flag type.
     * @param defaultValue The default value.
     * @throws IllegalArgumentException If the default value type does not match the flag type.
     */
    private fun validateDefaultValue(type: FlagType, defaultValue: DefaultValue) {
        when (type) {
            FlagType.BOOLEAN -> if (defaultValue !is DefaultValue.BooleanDefault) throw IllegalArgumentException(
                "Default value type does not match flag type BOOLEAN"
            )

            FlagType.STRING -> if (defaultValue !is DefaultValue.StringDefault) throw IllegalArgumentException(
                "Default value type does not match flag type STRING"
            )

            FlagType.NUMBER -> if (defaultValue !is DefaultValue.NumberDefault) throw IllegalArgumentException(
                "Default value type does not match flag type NUMBER"
            )

            FlagType.JSON -> if (defaultValue !is DefaultValue.JsonDefault) throw IllegalArgumentException(
                "Default value type does not match flag type JSON"
            )
        }
    }
}
