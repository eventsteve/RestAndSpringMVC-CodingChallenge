package com.github.vedenin.codingchallenge.mvc.controler;

import com.github.vedenin.codingchallenge.common.CurrencyEnum;
import com.github.vedenin.codingchallenge.converter.CurrentConvector;
import com.github.vedenin.codingchallenge.converter.DateConverter;
import com.github.vedenin.codingchallenge.mvc.model.ConverterFormModel;
import com.github.vedenin.codingchallenge.persistence.UserEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.math.BigDecimal;
import javax.inject.Inject;

import static com.github.vedenin.codingchallenge.mvc.Consts.*;
/*
* Controller that provide main page of Converter application
*/
@Controller
public class ConverterController extends WebMvcConfigurerAdapter {

    private static final String CURRENCY_ENUM = "currencyEnum";
    private static final String RESULT = "result";

    @Inject
    CurrentConvector currentConvector;
    @Inject
    DateConverter dateConverter;

    @GetMapping(CONVERTER_URL)
    public String showForm(ConverterFormModel converterFormModel, Model model) {
        model.addAttribute(CURRENCY_ENUM, CurrencyEnum.values());
        model.addAttribute(RESULT, "");
        return CONVERTER_URL;
    }

    @PostMapping(CONVERTER_URL)
    public String returnConverterResult(ConverterFormModel converterFormModel, Model model) {
        model.addAttribute(CURRENCY_ENUM, CurrencyEnum.values());
        if(converterFormModel.getType() != null) {
            BigDecimal result;
            if (converterFormModel.getType().equals("history")) {
                result = currentConvector.getConvertHistoricalValue(converterFormModel.getAmount(),
                        converterFormModel.getCurrencyEnumFrom(), converterFormModel.getCurrencyEnumTo(),
                        dateConverter.getCalendarFromString(converterFormModel.getDate()));
            } else {
                result = currentConvector.getConvertValue(converterFormModel.getAmount(),
                        converterFormModel.getCurrencyEnumFrom(),
                        converterFormModel.getCurrencyEnumTo());
            }
            model.addAttribute(RESULT, String.format("%.3f%n", result));
        } else {
            model.addAttribute(RESULT, "");
        }
        return CONVERTER_URL;
    }

    @RequestMapping(REGISTER_URL)
    public String showRegisterForm(UserEntity userEntity, Model model) {
        if(userEntity.getUserName() != null) {
            return LOGIN_URL;
        } else {
            return REGISTER_URL;
        }
    }


    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName(LOGIN_URL);
        registry.addViewController("/" + LOGIN_URL).setViewName(LOGIN_URL);
    }

}
