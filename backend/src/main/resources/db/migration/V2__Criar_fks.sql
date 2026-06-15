ALTER TABLE Evento ADD CONSTRAINT fk_evento_usuario FOREIGN KEY (Id_organizador) REFERENCES Usuario (Id_usuario) ON DELETE CASCADE; -- coloquei ON DELETE CASCADE porque se você apaga o Usuario que cria o evento, automaticamente vai apagar aquele evento

ALTER TABLE Categoria_evento ADD CONSTRAINT fk_categoria_evento FOREIGN KEY (Id_evento) REFERENCES Evento (Id_evento) ON DELETE CASCADE; -- aqui também, apagou o evento, apaga a relação dele com Categoria

ALTER TABLE Categoria_evento ADD CONSTRAINT fk_evento_categoria FOREIGN KEY (Id_categoria) REFERENCES Categoria (Id_categoria) ON DELETE CASCADE;

ALTER TABLE Participacao ADD CONSTRAINT fk_participacao_evento FOREIGN KEY (Id_evento) REFERENCES Evento (Id_evento) ON DELETE CASCADE;

ALTER TABLE Participacao ADD CONSTRAINT fk_participacao_usuario FOREIGN KEY (Id_usuario) REFERENCES Usuario (Id_usuario) ON DELETE CASCADE;

ALTER TABLE Avaliacao ADD CONSTRAINT fk_avaliacao_evento FOREIGN KEY (Id_evento) REFERENCES Evento (Id_evento) ON DELETE CASCADE;

ALTER TABLE Avaliacao ADD CONSTRAINT fk_avaliador FOREIGN KEY (Id_usuario_avaliador) REFERENCES Usuario (Id_usuario);

ALTER TABLE Avaliacao ADD CONSTRAINT fk_avaliado FOREIGN KEY (Id_usuario_avaliado) REFERENCES Usuario (Id_usuario);