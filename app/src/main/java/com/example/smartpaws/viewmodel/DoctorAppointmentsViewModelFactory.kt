import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.smartpaws.data.repository.AppointmentRepository
import com.example.smartpaws.data.repository.DoctorRepository
import com.example.smartpaws.viewmodel.DoctorAppointmentsViewModel

class DoctorAppointmentsViewModelFactory(
    private val appointmentRepository: AppointmentRepository,
    private val doctorRepository: DoctorRepository,
    private val userId: Long,
    private val userEmail: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DoctorAppointmentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DoctorAppointmentsViewModel(
                appointmentRepository,
                doctorRepository,
                userId,
                userEmail
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
