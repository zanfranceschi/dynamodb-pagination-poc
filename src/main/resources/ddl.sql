-- docker run --name banco -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123 -v /home/zanfranceschi/docker-volumes/mysql:/var/lib/mysql -d mysql:latest

CREATE TABLE `periodicidades` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `valor` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;


CREATE TABLE `saldos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cliente` varchar(100) NOT NULL,
  `payload` json NOT NULL,
  `periodicidadeId` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `saldos_FK` (`periodicidadeId`),
  CONSTRAINT `saldos_FK` FOREIGN KEY (`periodicidadeId`) REFERENCES `periodicidades` (`id`)
) ENGINE=InnoDB;


