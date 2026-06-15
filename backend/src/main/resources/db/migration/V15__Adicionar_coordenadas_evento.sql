ALTER TABLE Evento
ADD COLUMN IF NOT EXISTS Latitude double precision,
ADD COLUMN IF NOT EXISTS Longitude double precision,
ADD COLUMN IF NOT EXISTS Cidade varchar(120);
