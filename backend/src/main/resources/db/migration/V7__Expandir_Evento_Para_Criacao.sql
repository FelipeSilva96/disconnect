ALTER TABLE Evento
ALTER COLUMN Data_evento TYPE TIMESTAMP
USING Data_evento::timestamp;

ALTER TABLE Evento
ADD COLUMN Descricao varchar(500),
ADD COLUMN Url_foto varchar(300),
ADD COLUMN Dias_semana varchar(100),
ADD COLUMN Dias_mes varchar(100),
ADD COLUMN Quant_minima_pessoas int,
ADD COLUMN Quant_maxima_pessoas int,
ADD COLUMN Nivel_habilidade varchar(50),
ADD COLUMN Status varchar(30) DEFAULT 'Ativo';