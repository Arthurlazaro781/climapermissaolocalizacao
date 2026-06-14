# Clima Agora 🌤️ — Versão com Permissão Android

## Descrição
O **Clima Agora** é um aplicativo Android nativo que consome uma API pública para consultar informações climáticas em tempo real. Esta nova versão evolui o projeto integrando o serviço de GPS, permitindo que o usuário busque o clima da sua região atual de forma automática e segura, aplicando conceitos de *Runtime Permissions* do Android.

## Relação com a atividade anterior
Na primeira versão da atividade, o aplicativo realizava o consumo da API exclusivamente através de buscas manuais, onde o usuário precisava digitar o nome da cidade. Nesta evolução, a funcionalidade original de busca por texto foi totalmente mantida, e adicionamos uma nova camada funcional: um botão que utiliza as coordenadas reais do GPS do usuário para automatizar o consumo do mesmo endpoint da API pública.

## API utilizada
- **Nome da API:** wttr.in
- **Endpoint utilizado:** `/{cidade}?format=j1` (para buscas manuais) e `/{latitude},{longitude}?format=j1` (para buscas via GPS).
- **Dados exibidos no app:** Nome do Local, Temperatura atual (°C), Umidade (%) e Velocidade do Vento (km/h).

## Permissão Android utilizada
- **Permissão escolhida:** `ACCESS_FINE_LOCATION` (Localização Precisa).
- **Onde ela foi declarada no Manifest:** No arquivo `AndroidManifest.xml`, logo acima da tag `<application>`.
- **Por que essa permissão é necessária para o app:** Para acessar o hardware de GPS do dispositivo, obter a latitude e longitude exatas do usuário e repassar essas coordenadas na URL da API de clima.
- **Em qual momento do fluxo ela é solicitada ao usuário:** Exatamente no momento em que o usuário clica no botão "Usar Minha Localização" (permissão em tempo de execução).

Exemplo de declaração no Manifest:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
