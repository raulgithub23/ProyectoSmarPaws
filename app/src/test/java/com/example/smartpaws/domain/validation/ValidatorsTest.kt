package com.example.smartpaws.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ValidatorsTest {

    //EMAIL
    @Test
    fun validateEmail_valid_returnsNull() {
        val result = validateEmail("usuario@example.com")
        assertNull(result)
    }

    @Test
    fun validateEmail_empty_returnsError() {
        val result = validateEmail("")
        assertEquals("El email es obligatorio", result)
    }

    @Test
    fun validateEmail_invalidFormat_returnsError() {
        val result = validateEmail("usuario-sin-arroba")
        assertEquals("Formato de email inválido", result)
    }
    
    // LETRAS SOLO NAME
    @Test
    fun validateNameLettersOnly_valid_returnsNull() {
        val result = validateNameLettersOnly("Juan Pérez")
        assertNull(result)
    }

    @Test
    fun validateNameLettersOnly_empty_returnsError() {
        val result = validateNameLettersOnly("")
        assertEquals("El nombre es obligatorio", result)
    }

    @Test
    fun validateNameLettersOnly_withNumbers_returnsError() {
        val result = validateNameLettersOnly("Juan 123")
        assertEquals("Solo letras y espacios", result)
    }

    //TELEFONO DIGITOS
    @Test
    fun validatePhoneDigitsOnly_valid_returnsNull() {
        val result = validatePhoneDigitsOnly("912345678")
        assertNull(result)
    }

    @Test
    fun validatePhoneDigitsOnly_empty_returnsError() {
        val result = validatePhoneDigitsOnly("")
        assertEquals("El teléfono es obligatorio", result)
    }

    @Test
    fun validatePhoneDigitsOnly_withLetters_returnsError() {
        val result = validatePhoneDigitsOnly("9123abc")
        assertEquals("Solo números", result)
    }

    @Test
    fun validatePhoneDigitsOnly_invalidLength_returnsError() {
        val result = validatePhoneDigitsOnly("123") // Muy corto
        assertEquals("Debe tener entre 8 y 15 dígitos", result)
    }

    // CONTRA FUERTE
    @Test
    fun validateStrongPassword_valid_returnsNull() {
        val result = validateStrongPassword("Pass123$")
        assertNull(result)
    }

    @Test
    fun validateStrongPassword_empty_returnsError() {
        val result = validateStrongPassword("")
        assertEquals("La contraseña es obligatoria", result)
    }

    @Test
    fun validateStrongPassword_short_returnsError() {
        val result = validateStrongPassword("Pass1$")
        assertEquals("Mínimo 8 caracteres", result)
    }

    @Test
    fun validateStrongPassword_noUpperCase_returnsError() {
        val result = validateStrongPassword("pass123$")
        assertEquals("Debe incluir una mayúscula", result)
    }

    @Test
    fun validateStrongPassword_noLowerCase_returnsError() {
        val result = validateStrongPassword("PASS123$")
        assertEquals("Debe incluir una minúscula", result)
    }

    @Test
    fun validateStrongPassword_noDigit_returnsError() {
        val result = validateStrongPassword("PassWord$")
        assertEquals("Debe incluir un número", result)
    }

    @Test
    fun validateStrongPassword_noSymbol_returnsError() {
        val result = validateStrongPassword("Pass1234")
        assertEquals("Debe incluir un símbolo", result)
    }

    @Test
    fun validateStrongPassword_withSpaces_returnsError() {
        val result = validateStrongPassword("Pass 123$")
        assertEquals("No debe contener espacios", result)
    }

    // CONFIRMAR CONTRA
    @Test
    fun validateConfirm_matching_returnsNull() {
        val result = validateConfirm("Pass123$", "Pass123$")
        assertNull(result)
    }

    @Test
    fun validateConfirm_empty_returnsError() {
        val result = validateConfirm("Pass123$", "")
        assertEquals("Confirma tu contraseña", result)
    }

    @Test
    fun validateConfirm_mismatch_returnsError() {
        val result = validateConfirm("Pass123$", "Pass999$")
        assertEquals("Las contraseñas no coinciden", result)
    }

    // NOMBRE PET
    @Test
    fun validatePetName_valid_returnsNull() {
        val result = validatePetName("Bobby")
        assertNull(result)
    }

    @Test
    fun validatePetName_empty_returnsError() {
        val result = validatePetName("")
        assertEquals("El nombre es obligatorio", result)
    }

    @Test
    fun validatePetName_short_returnsError() {
        val result = validatePetName("A")
        assertEquals("Debe tener al menos 2 caracteres", result)
    }

    @Test
    fun validatePetName_invalidChars_returnsError() {
        val result = validatePetName("Bobby!")
        assertEquals("El nombre no debe contener números ni símbolos", result)
    }

    // FECHA FORMATO YYYY-MM-DD
    @Test
    fun validateDateFormat_valid_returnsNull() {
        val result = validateDateFormat("2000-01-01")
        assertNull(result)
    }

    @Test
    fun validateDateFormat_empty_returnsError() {
        val result = validateDateFormat("")
        assertEquals("La fecha es obligatoria", result)
    }

    @Test
    fun validateDateFormat_invalidFormat_returnsError() {
        val result = validateDateFormat("01-01-2000")
        assertEquals("Use el formato aaaa-mm-dd (Ej: 2023-05-20)", result)
    }

    @Test
    fun validateDateFormat_futureDate_returnsError() {
        val result = validateDateFormat("2099-12-31")
        assertEquals("La fecha no puede ser futura", result)
    }

    @Test
    fun validateDateFormat_invalidDay_returnsError() {
        val result = validateDateFormat("2023-02-30")
        assertEquals("Fecha inválida (ej: mes 13 o día 32)", result)
    }

    // NOTAS PET
    @Test
    fun validatePetNotes_valid_returnsNull() {
        val result = validatePetNotes("Notas cortas")
        assertNull(result)
    }

    @Test
    fun validatePetNotes_tooLong_returnsError() {
        val longNotes = "a".repeat(201)
        val result = validatePetNotes(longNotes)
        assertEquals("Las notas no pueden exceder los 200 caracteres", result)
    }

    // PESO PET
    @Test
    fun validatePetWeight_valid_returnsNull() {
        val result = validatePetWeight("15.5")
        assertNull(result)
    }

    @Test
    fun validatePetWeight_validComma_returnsNull() {
        val result = validatePetWeight("15,5")
        assertNull(result)
    }

    @Test
    fun validatePetWeight_empty_returnsError() {
        val result = validatePetWeight("")
        assertEquals("El peso es obligatorio", result)
    }

    @Test
    fun validatePetWeight_notANumber_returnsError() {
        val result = validatePetWeight("abc")
        assertEquals("Ingrese un número válido", result)
    }

    @Test
    fun validatePetWeight_zeroOrNegative_returnsError() {
        val result = validatePetWeight("0")
        assertEquals("El peso debe ser mayor a 0", result)
    }

    @Test
    fun validatePetWeight_tooHigh_returnsError() {
        val result = validatePetWeight("201")
        assertEquals("Verifique el peso (parece muy alto)", result)
    }

    //COLOR PET
    @Test
    fun validatePetColor_valid_returnsNull() {
        val result = validatePetColor("Marrón")
        assertNull(result)
    }

    @Test
    fun validatePetColor_empty_returnsError() {
        val result = validatePetColor("")
        assertEquals("El color es obligatorio", result)
    }

    @Test
    fun validatePetColor_short_returnsError() {
        val result = validatePetColor("Az")
        assertEquals("Sea más específico con el color", result)
    }

    @Test
    fun validatePetColor_invalidChars_returnsError() {
        val result = validatePetColor("Rojo1")
        assertEquals("Solo letras y espacios", result)
    }

    // CAMPO GENERICO NO VACIO
    @Test
    fun validateNotEmpty_valid_returnsNull() {
        val result = validateNotEmpty("Valor", "Campo")
        assertNull(result)
    }

    @Test
    fun validateNotEmpty_empty_returnsError() {
        val result = validateNotEmpty("", "Dirección")
        assertEquals("El campo 'Dirección' es obligatorio", result)
    }
}