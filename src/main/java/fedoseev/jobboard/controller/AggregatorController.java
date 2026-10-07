package fedoseev.jobboard.controller;

import fedoseev.jobboard.aggregator.ImportResult;
import fedoseev.jobboard.aggregator.VacancyImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Агрегатор", description = "Сбор вакансий с внешних сайтов")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/aggregator")
public class AggregatorController {

    private final VacancyImportService importService;

    @Operation(summary = "Собрать вакансии сейчас", description = "Только для ADMIN. Обходит все источники и возвращает итог по каждому.")
    @PostMapping("/run")
    public List<ImportResult> run() {
        return importService.importAll();
    }
}
