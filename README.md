[![Goover Logo](http://gooverbackend-gooverprd.rhcloud.com/images/logo.png)](http://www.gooverapp.com/)


#Goover Import

## Sobre

Goover é um aplicativo que demonstra a tendência de público para os diversos programas ou séries da TV (Aberta e Paga), Stream  e canais de Internet.

O Import é um aplicativo do tipo uber-jar que faz a importação de Programas para o Backend do Goover.

## O que o Import faz?

O robô de importação segue as seguintes ações:

* Lê uma lista de programas no arquivo texto listimport.txt
* Lê os arquivo de propriedades com a configurações de banco de dados de destino, categorias em que os programas da lista está inserido e os veículos onde passam os programas da lista
* Para cada programa da lista é feito uma pesquisa na API do site [http://api.themoviedb.org](http://api.themoviedb.org) esse API retorna um JSON REST com as informações do programa como título, generos, sinopse, imagem etc.
* É feito um upload local e temporário da imagem do programa e feito um upload no [Cloudinary.com](www.cloudinary.com).
* Essas informações são reunidas num documento JSON Programa e são inseridas no banco de dados.

## Pré Requisitos

Para instalar e executar é preciso ter as seguintes ferramentas instaladas:

* [MongoDB](www.mongodb.org) Caso queira fazer import numa base local
* [Java 1.8](www.java.com)
* [Maven](www.maven.apache.org)

## Instalação

O Goover Import foi construído sob um .project [Eclipse](www.eclipse.org).
Então basta fazer a importação do projeto Eclipse para o IDE local.

## Estrutura

Estrutura dos diretórios do robô:

```
/gooverimport
__/conf
__/file
__/img
__/log
__gooverimport.jar
```

### Descrição dos diretórios e recursos do robô:


#### Diretório ```/conf```

Diretório com a configuração do robô. Dentro dele terão 2 arquivos:


* ```config.properties```

Arquivo de Propriedades com as configurações do robô. Dentro das configurações estarão:

* URL do banco de dados Mongo destino
* IDs dos veiculos (onde passam os progamas, por exemplo HBO, Globo, SBT, Netflix etc.) separados por virgulas e sem espaços, por exemplo:
```5768a4b80b12fbe73f0c28a1,5768a4b80b12fbe73f0c2832```
* IDs das categorias onde estão inseridos os programas da lista também separados por vírgulas por exemplo:
```categorias=576896fb6d4a0f3c01e182cd,5768977a6d4a0f3c01e182cf```
* Chaves de API do Cloudinary e The Movie DB Api


* ```veiculos.txt```

Dentro do diretório terá um arquivo veiculos.txt para guia de consulta dos IDs dos veiculos para fazer a configuração do config.properties.


	

#### Diretório ```/file```

Arquivo ```listimport.txt```

Esse arquivo irá conter a lista de nomes dos programas que serão importadas para o Backend
O formato é uma listagem simples, por exemplo:
```
O Negócio
Parade's End
Penny Dreadful
```

#### Diretório ```/img```

Arquivo de imagem de download temporário. (não tem ação sobre ela)


#### Diretório ```/log```

Arquivos de logs de importação.

## Run

Você pode executar o robô com o simples comando:

```
java -jar gooverimport.jar
```

## Arquivos de Log:

Os arquivos de logs contém as informações das ações tomadas pela execução do robô. Como por exemplo conexão com o banco, programa importado, informa se o programa já existe na base etc.
Ao final do log é gerado um pequeno relatório estatístico como por exemplo:

```
----------------------------------------------
-------------   Estatísticas   ---------------
----------------------------------------------
-- Programas não encontrados -----------------
A Grande Luta
Programa Legal
-- Programas sem gêneros ---------------------
Programa Legal
-- Programas sem imagem ----------------------
Dios Inc.
-- Progrmas sem sinopse ----------------------
Two and a half men
----------------------------------------------
----------------------------------------------
```

## Contributors



## License