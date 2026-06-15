ALTER TABLE Evento
ADD COLUMN Data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- O evento tem uma data de criação la na classe Evento que eu esqueci de criar quando fiz a V1, ai to criando ela aqui