CREATE TABLE
    Usuario (
        Id_usuario SERIAL,
        Nome varchar(100) NOT NULL,
        Email varchar(150) UNIQUE NOT NULL,
        Senha varchar(30) NOT NULL,
        Idade int,
        Bio varchar(300),
        Url_foto varchar(300),
        Is_admin boolean DEFAULT false
    );

CREATE TABLE
    Evento (
        Id_evento SERIAL,
        Nome varchar(100) NOT NULL,
        Data_evento date NOT NULL,
        Localizacao varchar(100) NOT NULL,
        Frequencia varchar(20),
        Id_organizador int NOT NULL
    );

CREATE TABLE
    Categoria (
        Id_categoria SERIAL,
        Nivel varchar(20),
        Favoritar boolean,
        Modalidade varchar(20),
        Nome varchar(100) NOT NULL
    );

CREATE TABLE
    Categoria_evento (Id_evento int, Id_categoria int);

CREATE TABLE
    Participacao (
        Id_evento int,
        Id_usuario int,
        Status varchar(20) DEFAULT 'PENDENTE'
    );

CREATE TABLE
    Avaliacao (
        Id_evento int,
        Id_usuario_avaliador int,
        Id_usuario_avaliado int,
        Aperfil int,
        Aevento int
    );


ALTER TABLE Usuario ADD CONSTRAINT pk_usuario PRIMARY KEY (Id_usuario);

ALTER TABLE Evento ADD CONSTRAINT pk_evento PRIMARY KEY (Id_evento);

ALTER TABLE Categoria ADD CONSTRAINT pk_categoria PRIMARY KEY (Id_categoria);

ALTER TABLE Categoria_evento ADD CONSTRAINT pk_categoria_evento PRIMARY KEY (Id_evento, Id_categoria);

ALTER TABLE Participacao ADD CONSTRAINT pk_participacao PRIMARY KEY (Id_evento, Id_usuario);

ALTER TABLE Avaliacao ADD CONSTRAINT pk_avaliacao PRIMARY KEY (
    Id_evento,
    Id_usuario_avaliador,
    Id_usuario_avaliado
);