package pucp.grupo4d.modelo;

public enum TipoRuta {
    INTRACONTINENTAL(Problematica.MAX_DIAS_ENTREGA_INTRACONTINENTAL),
    INTERCONTINENTAL(Problematica.MAX_DIAS_ENTREGA_INTERCONTINENTAL);

    private final Double maxHorasParaEntrega;

    TipoRuta(Integer maxDiasParaEntrega) {
        this.maxHorasParaEntrega = 24.0*maxDiasParaEntrega;
    }

    public Double getMaxHorasParaEntrega() {
        return maxHorasParaEntrega;
    }
}
