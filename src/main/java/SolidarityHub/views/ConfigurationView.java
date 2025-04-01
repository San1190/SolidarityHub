package SolidarityHub.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin.Minus.Horizontal;
import SolidarityHub.models.Usuario;
import com.vaadin.flow.component.timepicker.TimePicker;
/*Imports del autogrid para lista de voluntariados 
import { AutoGrid } from '@vaadin/hilla-react-crud';
import ProductModel from 'Frontend/generated/com/vaadin/demo/fusion/crud/ProductModel';
import { ProductService } from 'Frontend/generated/endpoints';*/

@Route("configuracion")
public class ConfigurationView extends VerticalLayout {
    private VerticalLayout panelIzq;
    private VerticalLayout panelDer;

    private HorizontalLayout panelIzqSuperior;
    private HorizontalLayout panelIzqInferior;
    private HorizontalLayout panelDerSuperior;
    private HorizontalLayout panelDerInferior;

    private FormLayout panelForm;

    public ConfigurationView() {
        super();

        // Configuración del diseño principal
        setSizeFull();
        setSpacing(false);
        setPadding(false);

        this.panelIzq = new VerticalLayout();
        panelIzq.setWidth("50%");
        panelIzq.setAlignItems(Alignment.START);
        panelIzq.setJustifyContentMode(JustifyContentMode.START);

        this.panelDer = new VerticalLayout();
        panelDer.setWidth("50%");
        panelDer.setAlignItems(Alignment.END);
        panelDer.setJustifyContentMode(JustifyContentMode.END);

        this.panelIzqSuperior = new HorizontalLayout();
        this.panelIzqSuperior.setWidth("100%");
        this.panelIzqSuperior.setAlignItems(Alignment.CENTER);
        this.panelIzqSuperior.setJustifyContentMode(JustifyContentMode.CENTER);
        panelIzqSuperior.add(crearHorarioIni(), crearHorarioFin());

        this.panelIzqInferior = new HorizontalLayout();
        this.panelIzqInferior.setWidth("100%");
        this.panelIzqInferior.setAlignItems(Alignment.CENTER);
        this.panelIzqInferior.setJustifyContentMode(JustifyContentMode.CENTER);
        panelIzqInferior.add(crearListaVoluntariados());

        this.panelDerSuperior = new HorizontalLayout();
        this.panelDerSuperior.setWidth("100%");
        this.panelDerSuperior.setAlignItems(Alignment.CENTER);
        this.panelDerSuperior.setJustifyContentMode(JustifyContentMode.CENTER);
        panelDerSuperior.add(crearAvatar());

        this.panelDerInferior = new HorizontalLayout();
        this.panelDerInferior.setWidth("100%");
        this.panelDerInferior.setAlignItems(Alignment.CENTER);
        this.panelDerInferior.setJustifyContentMode(JustifyContentMode.CENTER);
        panelDerInferior.add(crearFormInfo());

        panelIzq.add(panelIzqSuperior, panelIzqInferior);
        panelDer.add(panelDerSuperior, panelDerInferior);

        add(panelIzq, panelDer);

    }

    private Component crearHorarioIni() {
        TimePicker timePicker = new TimePicker("Desde:");
        return timePicker;
    }

    private Component crearHorarioFin() {
        TimePicker timePicker = new TimePicker("Hasta:");
        return timePicker;
    }

    private Component crearAvatar() {
        // Si hay foto de perfil, cargarla
        Avatar user = new Avatar();
        user.setImage("Retornar foto de perfil");
        user.setName("Retornar nombre de usuario");
        return user;
    }

    private Component crearListaVoluntariados() {
        // return <AutoGrid service={ProductService} model={ProductModel} />;
        return new VerticalLayout();
    }

    private Component crearFormInfo() {
        FormLayout formLayout = new FormLayout();
        formLayout.setWidth("100%");
        formLayout.setHeight("100%");

        TextField nombreField = new TextField("Nombre:");
        TextField apellidosField = new TextField("Apellidos:");
        TextField emailField = new TextField("Email:");
        PasswordField passwordField = new PasswordField("Contraseña:");

        formLayout.add(nombreField, apellidosField, emailField, passwordField);

        return formLayout;
    }
}
