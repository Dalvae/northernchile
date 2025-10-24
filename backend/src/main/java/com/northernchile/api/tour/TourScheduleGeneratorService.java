package com.northernchile.api.tour;

import com.northernchile.api.external.LunarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TourScheduleGeneratorService {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    @Autowired
    private LunarService lunarService;

    // Se ejecuta todos los días a las 3 AM, por ejemplo.
    @Scheduled(cron = "0 0 3 * * ?")
    public void generateSchedules() {
        System.out.println("Running Tour Schedule Generator...");

        // TODO: Implementar la lógica completa del generador:
        // 1. Obtener todos los tours que son `isRecurring = true` y `status = 'PUBLISHED'`.
        //    List<Tour> recurringTours = tourRepository.findByIsRecurringAndStatus(true, "PUBLISHED");

        // 2. Iterar sobre un rango de fechas futuras (ej. los próximos 90 días).
        //    LocalDate today = LocalDate.now();
        //    for (int i = 0; i < 90; i++) {
        //        LocalDate date = today.plusDays(i);

        // 3. Para cada tour y cada fecha, verificar si la regla CRON del tour coincide con la fecha.
        //    (Esto requiere una librería o lógica para parsear CRON).

        // 4. Si coincide, verificar que no exista ya un TourSchedule para ese tour en esa fecha.
        //    `tourScheduleRepository.existsByTourIdAndStartDatetime(...)`

        // 5. Aplicar reglas de negocio: si es un tour astronómico, usar `lunarService.isFullMoon(date)`.

        // 6. Si todas las condiciones se cumplen, crear y guardar una nueva entidad `TourSchedule`.
        //    TourSchedule newSchedule = new TourSchedule();
        //    newSchedule.setTour(tour);
        //    ... (heredar valores del tour padre)
        //    tourScheduleRepository.save(newSchedule);
        // }
    }
}
