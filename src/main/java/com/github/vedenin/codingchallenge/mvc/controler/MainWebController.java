package com.github.vedenin.codingchallenge.mvc.controler;

import com.github.vedenin.codingchallenge.common.CurrencyEnum;
import com.github.vedenin.codingchallenge.converter.CurrencyConvector;
import com.github.vedenin.codingchallenge.converter.DateConverter;
import com.github.vedenin.codingchallenge.mvc.model.ConverterFormModel;
import com.github.vedenin.codingchallenge.mvc.model.CountryService;
import com.github.vedenin.codingchallenge.persistence.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.math.BigDecimal;
import java.util.Date;
import javax.inject.Inject;

import static com.github.vedenin.codingchallenge.mvc.Consts.*;

/*
* Controller that provide main page of Converter application
*/
@Controller
public class MainWebController extends WebMvcConfigurerAdapter {

    private static final String CURRENCY_ENUM = "currencyEnum";
    private static final String RESULT = "result";

    @Inject
    CurrencyConvector currencyConvector;
    @Inject
    DateConverter dateConverter;
    @Inject
    HistoryRepository historyRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    CountryService countryService;
    @Inject
    ErrorRepository errorRepository;
    @Inject
    PropertyService propertyService;

    @RequestMapping(CONVERTER_URL)
    public String handleConverterForm(ConverterFormModel converterFormModel, Model model, BindingResult bindingResult) {
        try {
            model.addAttribute(CURRENCY_ENUM, CurrencyEnum.values());
            if (converterFormModel.getAmount().doubleValue() > 0 && converterFormModel.getType() != null
                    && !bindingResult.hasErrors()) {
                BigDecimal result = getResult(converterFormModel);
                model.addAttribute(RESULT, String.format("%.3f%n", result));
                saveQuery(converterFormModel, result);
                converterFormModel.setType("current");
                converterFormModel.setDate("");
            } else {
                DefaultProperty property = propertyService.getDefaultProperties();
                converterFormModel.setAmount(property.getDefaultAmount());
                converterFormModel.setTo(property.getDefaultCurrencyTo());
                converterFormModel.setFrom(property.getDefaultCurrencyFrom());
                model.addAttribute(RESULT, "");
            }
            model.addAttribute("history", historyRepository.findFirst10ByOrderByDateCreateDesc());
            return CONVERTER_URL;
        } catch (Exception exp) {
            model.addAttribute(RESULT, "");
            model.addAttribute("error", exp.getMessage());
            return CONVERTER_URL;
        }
    }

    @RequestMapping(REGISTER_URL)
    public String handleRegisterForm(UserEntity userEntity, Model model) {
        try {
            if (userEntity.getUserName() != null && !userEntity.getUserName().isEmpty()) {
                if (userRepository.findByUserName(userEntity.getUserName()) != null) {
                    model.addAttribute("error", "This UserName already used");
                    model.addAttribute("countires", countryService.getCountriesNames());
                    return REGISTER_URL;
                } else {
                    userRepository.save(userEntity);
                    return LOGIN_URL;
                }
            } else {
                model.addAttribute("countires", countryService.getCountriesNames());
                return REGISTER_URL;
            }
        } catch (Exception exp) {
            model.addAttribute("error", exp.getMessage());
            return REGISTER_URL;
        }
    }


    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName(LOGIN_URL);
        registry.addViewController("/" + LOGIN_URL).setViewName(LOGIN_URL);
    }

    @ExceptionHandler(Exception.class)
    public String handleError(Model model, Exception ex) {
        errorRepository.save(new ErrorEntity("Error in MVC service", ex));
        model.addAttribute("error", ex.getMessage());
        return CONVERTER_URL;
    }

    private void saveQuery(ConverterFormModel converterFormModel, BigDecimal result) {
        historyRepository.save(new HistoryEntity(converterFormModel.getAmount(),
                converterFormModel.getCurrencyEnumFrom(), converterFormModel.getCurrencyEnumTo(),
                new Date(), result, converterFormModel.getType(), converterFormModel.getDate()));
    }

    private BigDecimal getResult(ConverterFormModel converterFormModel) {
        return currencyConvector.getConvertingValue(
                converterFormModel.isHistory(),
                converterFormModel.getAmount(),
                converterFormModel.getCurrencyEnumFrom(),
                converterFormModel.getCurrencyEnumTo(),
                dateConverter.getCalendarFromString(converterFormModel.getDate())
        );
    }
}
