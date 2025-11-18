package com.example.smartpaws.domain.validation

import android.util.Patterns

fun validateEmail(email: String): String? {                            // Retorna String? (mensaje) o null si está OK
    if (email.isBlank()) return "El email es obligatorio"              // Regla 1: no vacío
    val ok = Patterns.EMAIL_ADDRESS.matcher(email).matches()           // Regla 2: coincide con patrón de email
    return if (!ok) "Formato de email inválido" else null              // Si no cumple, devolvemos mensaje
}

// Valida que el nombre contenga solo letras y espacios (sin números)
fun validateNameLettersOnly(name: String): String? {                   // Valida nombre
    if (name.isBlank()) return "El nombre es obligatorio"              // Regla 1: no vacío
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")                      // Regla 2: solo letras y espacios (con tildes/ñ)
    return if (!regex.matches(name)) "Solo letras y espacios" else null// Mensaje si falla
}

// Valida que el teléfono tenga solo dígitos y una longitud razonable
fun validatePhoneDigitsOnly(phone: String): String? {                  // Valida teléfono
    if (phone.isBlank()) return "El teléfono es obligatorio"           // Regla 1: no vacío
    if (!phone.all { it.isDigit() }) return "Solo números"             // Regla 2: todos dígitos
    if (phone.length !in 8..15) return "Debe tener entre 8 y 15 dígitos" // Regla 3: tamaño razonable
    return null                                                        // OK
}

// Valida seguridad de la contraseña (mín. 8, mayús, minús, número y símbolo; sin espacios)
fun validateStrongPassword(pass: String): String? {                    // Requisitos mínimos de seguridad
    if (pass.isBlank()) return "La contraseña es obligatoria"          // No vacío
    if (pass.length < 8) return "Mínimo 8 caracteres"                  // Largo mínimo
    if (!pass.any { it.isUpperCase() }) return "Debe incluir una mayúscula" // Al menos 1 mayúscula
    if (!pass.any { it.isLowerCase() }) return "Debe incluir una minúscula" // Al menos 1 minúscula
    if (!pass.any { it.isDigit() }) return "Debe incluir un número"         // Al menos 1 número
    if (!pass.any { !it.isLetterOrDigit() }) return "Debe incluir un símbolo" // Al menos 1 símbolo
    if (pass.contains(' ')) return "No debe contener espacios"          // Sin espacios
    return null                                                         // OK
}

// Valida que la confirmación coincida con la contraseña
fun validateConfirm(pass: String, confirm: String): String? {          // Confirmación de contraseña
    if (confirm.isBlank()) return "Confirma tu contraseña"             // No vacío
    return if (pass != confirm) "Las contraseñas no coinciden" else null // Deben ser iguales
}

// --- VALIDADORES PARA MASCOTAS ---

/**
 * Valida el nombre de la mascota.
 * 1. No vacío.
 * 2. Mínimo 2 caracteres.
 * 3. Solo letras y espacios (sin números ni símbolos extraños).
 */
fun validatePetName(name: String): String? {
    if (name.isBlank()) return "El nombre es obligatorio"
    if (name.length < 2) return "Debe tener al menos 2 caracteres"
    // Regex: Permite letras (incluyendo tildes/ñ) y espacios.
    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")
    if (!regex.matches(name)) return "El nombre no debe contener números ni símbolos"
    return null
}

/**
 * Valida el formato de fecha (YYYY-MM-DD).
 * Esto es crucial para que la base de datos (SQLite/Room) pueda ordenarlas correctamente.
 */
fun validateDateFormat(date: String): String? {
    if (date.isBlank()) return "La fecha es obligatoria"

    // Regex simple para formato yyyy-mm-dd
    val regex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    if (!regex.matches(date)) return "Use el formato aaaa-mm-dd (Ej: 2023-05-20)"

    return null
}

/**
 * Valida el peso.
 * 1. No vacío.
 * 2. Debe ser numérico.
 * 3. Debe ser mayor a 0 y razonable (menos de 200kg para mascotas domésticas comunes).
 */
fun validatePetWeight(weight: String): String? {
    if (weight.isBlank()) return "El peso es obligatorio"

    val weightNumber = weight.toFloatOrNull() ?: return "Ingrese un número válido (use punto para decimales)"

    if (weightNumber <= 0) return "El peso debe ser mayor a 0"
    if (weightNumber > 200) return "Verifique el peso (parece muy alto)"

    return null
}

/**
 * Valida el color.
 * 1. No vacío.
 * 2. Solo letras (similar al nombre).
 */
fun validatePetColor(color: String): String? {
    if (color.isBlank()) return "El color es obligatorio"
    if (color.length < 3) return "Sea más específico con el color"

    val regex = Regex("^[A-Za-zÁÉÍÓÚÑáéíóúñ ]+$")
    if (!regex.matches(color)) return "Solo letras y espacios"

    return null
}

/**
 * Valida las notas (Opcional).
 * Si el usuario escribe algo, controlamos que no sea excesivamente largo.
 */
fun validatePetNotes(notes: String): String? {
    if (notes.isNotBlank() && notes.length > 200) {
        return "Las notas no pueden exceder los 200 caracteres"
    }
    return null
}
/**
 * Valida que un campo generico (Cualquiera).
 * 1. No debe estar vacía.
 */
fun validateNotEmpty(field: String, fieldName: String): String? {
    if (field.isBlank()) return "El campo '$fieldName' es obligatorio"
    return null
}