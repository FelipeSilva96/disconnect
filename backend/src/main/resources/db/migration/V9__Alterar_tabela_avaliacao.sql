
ALTER TABLE Avaliacao DROP CONSTRAINT IF EXISTS fk_avaliacao_evento;
ALTER TABLE Avaliacao DROP CONSTRAINT IF EXISTS fk_avaliador;
ALTER TABLE Avaliacao DROP CONSTRAINT IF EXISTS fk_avaliado;


ALTER TABLE Avaliacao DROP CONSTRAINT IF EXISTS pk_avaliacao;


ALTER TABLE Avaliacao 
    DROP COLUMN Id_usuario_avaliado,
    DROP COLUMN Aperfil,
    DROP COLUMN Aevento;


ALTER TABLE Avaliacao RENAME COLUMN Id_usuario_avaliador TO Id_avaliador;


ALTER TABLE Avaliacao 
    ADD COLUMN Id_avaliacao SERIAL PRIMARY KEY,
    ADD COLUMN Nota INT NOT NULL DEFAULT 5, 
    ADD COLUMN Comentario VARCHAR(300),
    ADD COLUMN Data_avaliacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP;


ALTER TABLE Avaliacao ADD CONSTRAINT chk_nota CHECK (Nota >= 1 AND Nota <= 5);


ALTER TABLE Avaliacao 
    ADD CONSTRAINT fk_avaliacao_avaliador FOREIGN KEY (Id_avaliador) REFERENCES Usuario (Id_usuario) ON DELETE CASCADE,
    ADD CONSTRAINT fk_avaliacao_evento FOREIGN KEY (Id_evento) REFERENCES Evento (Id_evento) ON DELETE CASCADE;