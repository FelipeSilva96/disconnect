ALTER TABLE Avaliacao
ADD COLUMN IF NOT EXISTS Id_avaliado int;

ALTER TABLE Avaliacao
ADD CONSTRAINT fk_avaliacao_avaliado FOREIGN KEY (Id_avaliado) REFERENCES Usuario (Id_usuario) ON DELETE CASCADE;