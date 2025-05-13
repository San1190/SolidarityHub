package SolidarityHub.models;

public interface Observado {

    void suscribirObservador(Observador observador);
    void dessuscribirObservador(Observador observador);
    void notificarSuscriptores(); //Implementado en el servicio
}
