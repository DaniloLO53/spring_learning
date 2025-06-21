<h1>Entendendo a arquitetura dos filtros de segurança do Spring Security</h1>

<p>
    Primeiro, precisamos entender a diferença e as associações entre o mundo do Spring  (a classe <code>ApplicationContext</code>) que gerencia o ciclo de vida dos Beans e o mundo do <strong>Tomcat</strong>. O Tomcat é o container de servlets mais utilizado, sendo inclusive utilizado pelo Spring Boot. É ele quem gerencia o ciclo de vida e os filtros de cada servlet. Servlet, por sua vez, é uma classe que estende a classe <code>HttpServlet</code> e recebe as requisições administradas pelo Tomcat vindas do cliente. O Tomcat distribui a requisição para o seu servlet correspondente a partir da URL configurada. Os dois mundos não possuem nada em comum: um não sabe da existência do outro e não compartilham nenhuma classe ou configuração.
</p>
<p>
    <strong>O problema:</strong> os filtros de segurança, que são beans implementados pelo Spring (desenvolvidos pelo desenvolvedor) precisam ser adicionados à cadeia de filtros do Tomcat. Porém, como o Tomcat irá chamar um filtro que não conhece? É aí que entra o <code>DelegatingFilterProxy</code>.
</p>

<h2>1. As 3 camadas dos filtros de segurança</h2>

<ul>
<li>
<h3>Camada 1: <code>DelegatingFilterProxy</code></h3> 

<p>O módulo spring-web contém um inicializador que implementa <code>ServletContainerInitializer</code> que registra uma instância de <code>DelegatingFilterProxy</code> (antigamente, isso era feito via web.xml). Quando o Tomcat inicia, ele escaneia os JARs da aplicação em busca de implementações dessa interface padrão. Ele encontra o inicializador do Spring e o executa. A requisição passa pelos filtros do Tomcat até chegar no filtro <code>DelegatingFilterProxy</code>. Essa classe, apesar de fazer parte do Spring, é conhecida pelo Tomcat, pois implementa a interface <code>Filter</code> que é do Tomcat. Para o Tomcat, essa classe é apenas mais um filtro que segue as especificações de servlet.</p>
<p>A classe <code>DelegatingFilterProxy</code> não possui nenhuma lógica de segurança. A sua única função é procurar no <code>ApplicationContext</code> por um bean chamado <code>springSecurityFilterChain</code> (do tipo <code>FilterChainProxy</code>) e delegar a requisição para ele através do <code>doFilter()</code>.
</p>
</li>

<li>
<h3>Camada 2: <code>FilterChainProxy</code></h3>

<p>
Essa interface funciona como o cérebro que cuida das cadeias de segurança (<code>SecurityFilterChain</code>). A função desse bean não é cuidar da lógica de segurança em si, mas escolher qual <code>SecurityFilterChain</code> irá lidar com cada requisição. Dessa forma, diferentes partes da aplicação podem possuir diferentes lógicas de segurança (diferentes cadeias de segurança).
</p>

<p>
A <code>FilterChainProxy</code> mantém uma lista de <code>SecurityFilterChain</code> e cada cadeia está associada a um <code>RequestMatcher</code>, que valida um padrão de URL. Quando uma requisição chega da classe <code>DelegatingFilterProxy</code>, o  <code>FilterChainProxy</code> a inspeciona e a compara com o <code>RequestMatcher</code> de cada <code>SecurityFilterChain</code> em sua lista, na ordem (que pode ser configurada com a annotation <code>@Order</code>. Assim que ele encontra a primeira <code>SecurityFilterChain</code> cujo padrão de URL corresponde à requisição, ele para de procurar e passa a requisição para essa cadeia específica de filtros. Os filtros dessa cadeia (e somente essa) serão executados. Se nenhuma cadeia corresponder, a requisição passa sem filtros de segurança (geralmente, resultando em um erro 404 ou acesso negado, dependendo da configuração).
</p>
</li>

<li>
<h3>Camada 3: <code>SecurityFilterChain</code></h3>

<p>
O Spring Boot coleta automaticamente todos esses beans <code>SecurityFilterChain</code>, os coloca dentro de um único <code>FilterChainProxy</code> e nomeia esse bean mestre como <strong>springSecurityFilterChain</strong>. É exatamente este nome que o <code>DelegatingFilterProxy</code> procura por padrão.
</p>

<p>
Cada bean <code>SecurityFilterChain</code> é, geralmente, associado a um <strong>securityMatcher</strong>. Isso informa ao <code>FilterChainProxy</code> quando esta cadeia específica deve ser usada. Por exemplo, uma cadeia pode ser para <strong>/api/**</strong> e outra para o resto da aplicação.
</p>
<p>
Cada bean <code>SecurityFilterChain</code> possui os mecanismos de segurança a serem utilizados na requisição, a qual possui uma URL específica que dá match com a URL configurada para a cadeia de filtros. Quando uma requisição chega ao <code>FilterChainProxy</code>, ele a analisa e diz: "Ok, essa requisição é para <strong>/admin/dashboard</strong>. Conforme as minhas regras, a camada responsável por lidar com admin (<code>SecurityFilterChain</code> para <strong>/admin/**</strong>) é responsável por isso. A partir daqui, essa camada assume!". Nesse momento, a requisição é entregue ao primeiro filtro da <code>SecurityFilterChain</code> de admin, que a processa e a passa para o próximo filtro na esma cadeia, e assim por diante.
</p>
</li>
</ul>

<h2>2. O filtro de segurança</h2>
<p>
Cada filtro de segurança pode ser utilizado para diversas funcionalidades como cors, segurança contra exploits, autenticação, autorização, etc. Podemos, ainda, criar o nosso próprio filtro de segurança. Para isso, basta criar uma classe que estenda <code>Filter</code> ou sua versão especializadas para requisições HTTP <code>OncePerRequestFilter</code>: 
</p>

<pre>
<code>
public class RobotAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!Collections.list(request.getHeaderNames()).contains("x-robot-secret")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!request.getHeader("x-robot-secret").equals("beep-boop")) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().println("Forbidden...");
            return;
        }

        RobotAuthenticationToken auth = new RobotAuthenticationToken();
        SecurityContext newContext = SecurityContextHolder.createEmptyContext();
        newContext.setAuthentication(auth);
        SecurityContextHolder.setContext(newContext);

        filterChain.doFilter(request, response);
    }
}
</code>
</pre>

<p>
Nesse caso, é o filtro que irá conter a lógica responsável pela segurança. Podemos adicionar o filtro na cadeia de filtros de segurança assim:
</p>

<pre>
<code>
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(Customizer.withDefaults())
            .httpBasic(Customizer.withDefaults())
            .formLogin(Customizer.withDefaults())
            .addFilterBefore(new RobotAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
</code>
</pre>

<p>
Veja que utilizamos o método <code>addFilterBefore()</code>. Esse método adiciona um filtro antes de outro filtro que, nesse caso, é o filtro <code>UsernamePasswordAuthenticationFilter</code>, um filtro padrão para autenticação com username e password.
</p>

<h2>3. A arquitetura de autenticação no Spring Security</h2>

<h3>3.1. A classe de contexto global: <code>SecurityContextHolder</code></h3>

<p>
Essa é a classe responsável por armazenar todos os dados relevantes que caracterizam um usuário como autenticado. O Spring Security confia nessa classe para saber quem (ou o que) está autenticado ou não. Portanto, o Spring Security não se preocupa em como essa classe é populada; se a classe contém um valor, esse valor seŕa usado como um usuário autenticado.
</p>

<p>
Podemos usar essa classe diretamente para dizer se um usuário está autenticado:
</p>

<pre>
<code>
SecurityContext context = SecurityContextHolder.createEmptyContext();
Authentication authentication = new TestingAuthenticationToken("username", "password", "ROLE_USER");
context.setAuthentication(authentication);

SecurityContextHolder.setContext(context);
</code>
</pre>

<ol>
<li>
Criamos um contexto vazio a partir do método estático <code>SecurityContextHolder.createEmptyContext()</code>. É importante criar um contexto vazio, em vez de fazer <code>SecurityContextHolder.getContext().setAuthentication(authentication)</code>. Essa prática evita "race conditions" em múltiplas threads.
</li>
<br/>
<li>
Criamos uma instância de uma autenticação (<code>Authentication</code>, a qual ainda iremos discutir).
</li>
<br/>
<li>
Adicionamos o objeto de autenticação ao contexto.
</li>
<br/>
<li>
Adicionamos o contexto ao <code>SecurityContextHolder</code>, que é responsável por armazenar o contexto de autenticação.
</li>
</ol>

<p>
Para acessar o objeto autenticado, podemos fazer:
</p>

<pre>
<code>
SecurityContext context = SecurityContextHolder.getContext();
Authentication authentication = context.getAuthentication();
String username = authentication.getName();
Object principal = authentication.getPrincipal();
Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
</code>
</pre>

<h3>3.2. A classe que representa a autenticação: <code>Authentication</code></h3>

<p>
Essa classe possui dois sentidos: ela pode ser ou uma entidade já autenticada, ou uma série de dados que ainda serão validados e identificados (ou não) como uma entidade legítima que quer se autenticar.
</p>
<p>
Caso seja uma entidade já autenticada, podemos obtê-la a partir do <code>SecurityContextHolder</code>, como fizemos no exemplo acima. Caso seja uma entidade a ser autenticada, podemos passá-la para <code>AuthenticationManager</code>, que ainda falaremos. Nesse caso, <code>isAuthenticated()</code> retorna falso.
</p>
<p>
Por padrão, objetos do tipo <code>Authentication</code> possuem <strong>"Token"</strong> ao final do nome, como, por exemplo, <code>UsernamePasswordAuthenticationToken</code>.
</p>
<p>
Um Authentication possui 3 objetos relevantes:
</p>
<ol>
<li>
<code>principal:</code> é o que identifica o usuário. Quando se trata de uma autenticação com username/password, geralmente é uma instância de <code>UserDetails</code>.
</li>
<br/>
<li>
<code>credentials:</code> geralmente é o password. Em muitos casos é limpo após o usuário ser autenticado para evitar vazamentos.
</li>
<br/>
<li>
<code>authorities:</code> instâncias da classe <code>GrantedAuthority</code> que cuidam das permissões do usuário (scope e role).
</li>
</ol>

<p>
A classe <code>GrantedAuthority</code> pode ser obtida através de <code>Authorization.getAuthorities()</code> que retorna uma coleção dessa classe. Ela representa uma autoridade designada ao <code>principal</code> e, geralmente, trata-se de "roles" como <strong>"ROLE_user", "ROLE_admin"</strong>, etc.
</p>
<p>
Usualmente, as instâncias de <code>GrantedAuthority</code> são carregadas por meio de implementações da interface <code>UserDetailsService</code> 
</p>

<h3>3.3. A API geral de autenticação: <code>AuthenticationManager</code></h3>

<p>
Essa é uma classe genérica responsável por definir como o Spring Security lida com autenticação. A autenticação (instância <code>Authentication</code>) retornada é passada para o <code>SecurityContextHolder</code>. Entretanto, geralmente essa classe não é implementada diretamente pelo desenvolvedor. A implementação mais comum para essa classe é a <code>ProviderManager</code>.
</p>

<h3>3.4. O gerente dos processos de autenticação: <code>ProviderManager</code></h3>

<p>
É a implementação mais comum de <code>AuthenticationManager</code>. A sua função é orquestrar os processos de autenticação realizados pelo <code>AuthenticationProvider</code>. Cada <code>AuthenticationProvider</code> é responsável por um tipo de autenticação: podemos ter um <code>AuthenticationProvider</code> que cuida da autenticação por formulário (com DaoAuthenticationProvider) e um que cuida da autenticação por token de API (com seu <code>JwtAuthenticationProvider</code> customizado).
</p>

<p>
Portanto, essa classe não possui qualquer lógica de autenticação, mas mantém uma lista de <code>AuthenticationProvider</code> - classes especializadas em tipos de autenticação específicos. Para cada autenticação que chega, a classe verifica qual é a <code>AuthenticationProvider</code> responsável por lidar com aquele tipo de autenticação.
</p>

<h3>3.5. O autenticador especialista: <code>AuthenticationProvider</code></h3>

<p>
Trata-se de uma interface que define um contrato para processar um tipo específico de autenticação. Sua responsabilidade é receber um objeto <code>Authentication</code> (com as credenciais do usuário), validá-lo e retornar um novo objeto <code>Authentication</code> totalmente preenchido e autenticado, ou lançar uma exceção se a autenticação falhar.
</p>

<p>
Dessa forma, cada classe encapsula uma lógica de autenticação, tornando o sistema modular. O <code>DaoAuthenticationProvider</code>, por exemplo, é a implementação padrão do Spring Security que sabe como autenticar usando um <code>UserDetailsService</code> (para buscar no banco) e um <code>PasswordEncoder</code> (para comparar senhas). Para autenticar com JWT, criamos o nosso próprio <code>AuthenticationProvider</code>.
</p>

<p>
Esse contrato possui duas implementações principais e obrigatórias:
</p>

<ol>
<li>
<code>Authentication authenticate(Authentication authentication):</code> O método principal onde a lógica de validação acontece.
</li>
<br/>
<li>
<code>boolean supports(Class<?> authentication):</code> Um método crucial onde o Provider diz se ele sabe ou não lidar com um determinado tipo de objeto <code>Authentication</code>. O <code>DaoAuthenticationProvider</code>, por exemplo, só suporta <code>UsernamePasswordAuthenticationToken</code>. É aqui onde reside a especialidade de cada Provider; é o método utilizado pelo <code>ProviderManager</code> para saber se o Provider é o especialista responsável por determinada autenticação.
</li>
</ol>

<h4>3.5.1. Olhando um pouco mais de perto o <code>boolean supports(Class<?> authentication):</code></h4>

<ol>
<li>
Um usuário envia um login e senha através de um formulário. Um filtro do Spring Security (como o <code>UsernamePasswordAuthenticationFilter</code>) intercepta isso e cria um objeto do tipo <code>UsernamePasswordAuthenticationToken</code>.
</li>
<br/>
<li>
Esse token é passado para o <code>ProviderManager</code>.
</li>
<br/>
<li>
O <code>ProviderManager</code> então inicia a sua "entrevista" com os especialistas:
</li>
<ul>
<li>
Ele chega no seu <code>JwtAuthenticationProvider</code> (caso exista um) e pergunta: <code>jwtProvider.supports(UsernamePasswordAuthenticationToken.class)</code>?
</li>
<li>
O <code>JwtAuthenticationProvider</code> provavelmente responderá false, porque ele só foi programado para lidar com um <code>JwtAuthenticationToken</code>.
</li>
<li>
Ele continua e chega no <code>DaoAuthenticationProvider</code> (o especialista padrão para login/senha) e pergunta: <code>daoProvider.supports(UsernamePasswordAuthenticationToken.class)</code>?
</li>
<li>
O <code>DaoAuthenticationProvider</code> responderá <code>true</code>, pois ele foi projetado exatamente para isso.
</li>
</ul>
<br/>
<li>
Como a resposta foi true, o <code>ProviderManager</code> para de procurar e delega o trabalho, chamando <code>daoProvider.authenticate(token)</code>.
</li>
</ol>

<h3>3.6. Exemplo de fluxo da requisição</h3>

<p>
Para internalizar o que aprendemos até aqui, vamos dar uma olhada no fluxo de uma requisição:
</p>

<ol>
<li>
<strong>A Requisição Chega:</strong> O <code>AuthenticationManager</code> (que geralmente é uma instância de <code>ProviderManager</code>) recebe um objeto <code>Authentication</code> não autenticado (ex: um <code>UsernamePasswordAuthenticationToken</code> vindo de um formulário de login).
</li>
<br/>
<li>
<strong>O Gerente Pergunta:</strong> O <code>ProviderManager</code> começa a iterar sobre a sua lista de <code>AuthenticationProviders</code>.
</li>
<br/>
<li>
<strong>Busca pelo Especialista:</strong> Para cada provider na lista, o <code>ProviderManager</code> pergunta: "provider, você sabe lidar com esse tipo de token?" (chamando o método <code>provider.supports(token.getClass())).</code>
</li>
<br/>
<li>
<strong>Especialista Encontrado</strong>: Assim que um provider responde true, o <code>ProviderManager</code> para de procurar e entrega o token para esse especialista, dizendo: "Valide isso para mim!" (chamando o método <code>provider.authenticate(token))</code>.
</li>
<br/>
<li>
<strong>Validação</strong>:
<ul>
<li>
Se o provider conseguir autenticar com sucesso, ele retorna um objeto <code>Authentication</code> totalmente preenchido <code>(authenticated = true).</code> O <code>ProviderManager</code> recebe esse objeto e o retorna como resultado final do processo. Fim da história.
</li>
<br/>
<li>
Se o provider falhar (ex: senha incorreta) e lançar uma <code>AuthenticationException</code>, o <code>ProviderManager</code> pode, dependendo da configuração, continuar e tentar o próximo provider na lista que também suporte o mesmo tipo de token.
</li>
</ul>
</li>
<br/>
<li>
<strong>Nenhum Especialista Encontrado:</strong> Se o <code>ProviderManager</code> percorrer toda a lista e nenhum provider suportar o tipo de <code>Authentication</code> token fornecido, ele lançará uma <code>ProviderNotFoundException</code>.
</li>
</ol>

<h2>4. Autenticação com username / password</h2>

<p>
Estudamos a arquitetura dos filtros servlets e a dos filtros de segurança com os seus principais agentes e camadas. Agora, vamos dar uma olhada em uma implementação comum do Spring Security para autenticações que exigem password e username. 
</p>

<h3>4.1. <code>UserDetails</code> e <code>UserDetailsService</code></h3>

<p>
O <code>UserDetails</code>, como ja vimos, é a classe que representa as informações de autenticação da entidade em um formato que o Spring Security entende e contém dados como o password encriptado e as permissões. Ela faz parte da classe <code>Authentication</code> após ser populada por meio do <code>UserDetailsService</code> (quando a autenticação é bem sucedida). Essa última classe, por sua vez, é a que contém a lógica de busca das informações do usuário - ela não conhece nada sobre a lógica de segurança; dado um <code>username</code>, ela apenas busca o usuário no banco de dados e retorna uma instância da classe <code>UserDetails</code> por meio do método <code>loadUserByUsername(String username)</code>. Logo, o <code>UserDetails</code> funciona como um <strong>DTO</strong> - um formato de dado reconhecível pelo Spring Security.
</p>
<p>
Assim, entendemos que <code>UserDetailsService</code> serve como uma camada de abstração para a lógica de consulta ao banco de dados. Isso torna o Spring Security modular, pois ele não se importa com a fonte dos dados (se é MongoDB, MySQL, etc) e nem com a consulta - ele apenas confia ao <code>UserDetailsService</code> a responsabilidade de lidar com essa tarefa e espera receber um objeto de <code>UserDetails</code> para ser adicionado ao <code>Authentication</code>. 
</p>
<p>
Surge, agora, a seguinte dúvida: vimos que o Spring Security lida com a autenticação por meio do <code>AuthenticationProvider</code> e existem várias implementações para essa classe. Qual seria uma implementação responsável por lidar com autenticações que exigem username e password? A resposta é: <code>DaoAuthenticationProvider</code>. 
</p>

<h3>4.2. Entendendo um pouco melhor o <code>UserDetails</code></h3>

<p>
 O <code>UserDetails</code> é criado <strong>antes</strong> que a autenticação seja confirmada, mas ele vive e representa o usuário <strong>após</strong> a autenticação ser bem-sucedida. Podemos fazer uma analogia com um check-in em um aeroporto
</p>
<ol>
<li>
<strong>Você (Usuário):</strong> Chega e diz seu nome (username) e entrega seu passaporte (password).
</li>
<br/>
<li>
<strong>Atendente (<code>AuthenticationProvider</code>):</strong> Ele pega seu nome para te encontrar no sistema.
</li>
<br/>
<li>
<strong>Sistema da Companhia Aérea (<code>UserDetailsService</code>):</strong> O atendente digita seu nome no sistema, que busca e retorna sua reserva completa.
</li>
<br/>
<li>
<strong>A Reserva Completa <code>(UserDetails):</code></strong> Este é o <code>UserDetails</code>. É o conjunto de dados "oficiais" sobre você que está no sistema da companhia: seu nome completo, número do passaporte, status do voo, etc. Note que isso acontece ANTES de você ser oficialmente "autenticado" (ter seu check-in concluído). A reserva é a matéria-prima para a verificação.
</li>
<br/>
<li>
<strong>A Verificação:</strong> O atendente agora compara o passaporte que você entregou na mão com o número do passaporte que está na sua reserva (<code>UserDetails</code>).
</li>
<br/>
<li>
<strong>O Cartão de Embarque (<code>Authentication</code> autenticado):</strong> Se tudo bater, ele te entrega o cartão de embarque. Este cartão confirma que sua identidade foi validada com sucesso e, crucialmente, ele está vinculado à sua reserva original (<code>UserDetails</code>).
</li>
</ol>

<p>
Agora que já entendemos o roteiro da autenticação e o papel do <code>UserDetails</code>, vamos ver o fluxo técnico da autenticação dentro do Spring Security
</p>

<ol>
<li>
<p>
<strong>O "ANTES": Matéria-Prima para a Validação</strong>
</p>
<p>
Quando um usuário tenta se logar, o <code>AuthenticationProvider</code> (geralmente o <code>DaoAuthenticationProvider</code>) precisa verificar se a senha fornecida está correta. Mas, correta em relação a quê? Em relação à senha que está armazenada no seu banco de dados.
</p>
<p>
Para obter essa informação, ele faz o seguinte:
</p>
<ul>
<li>
Ele pega o username que o usuário digitou.
</li>
<br/>
<li>
Chama o método <code>loadUserByUsername(username)</code> do seu <code>UserDetailsService</code>.
</li>
<br/>
<li>
Seu <code>UserDetailsService</code> vai ao banco, busca o usuário e cria um objeto <code>UserDetails</code> com os dados armazenados (username, senha codificada, papéis, etc.).
</li>
</ul>
<p>
Neste momento, o <code>UserDetails</code> foi criado, mas a autenticação ainda não foi concluída. Ele serve como a "fonte da verdade" contra a qual a tentativa de login será comparada.
</p>
</li>
<br>
<li>
<p>
<strong>O "DEPOIS": A Identidade do Usuário Autenticado</strong>
</p>
<p>
Agora, o <code>AuthenticationProvider</code> pega a senha que o usuário digitou, a codifica e a compara com a senha obtida de <code>userDetails.getPassword().</code>
</p>
<ul>
<li>
Se a autenticação falhar, o objeto <code>UserDetails</code> é descartado e uma exceção é lançada.
</li>
<br/>
<li>
Se a autenticação for bem-sucedida, o Spring Security cria um novo objeto <code>Authentication</code> (o "cartão de embarque"), que representa a sessão autenticada do usuário. E o mais importante: o <code>principal</code> (a identidade principal) dentro deste novo objeto <code>Authentication</code> é o próprio objeto <code>UserDetails</code> que foi carregado anteriormente.
</li>
</ul>
<p>
Esse objeto <code>Authentication</code> é então colocado no <code>SecurityContextHolder</code>, onde representa o usuário logado durante toda a sua sessão.
</p>
</li>
</ol>

<p>
Portanto, o <code>UserDetails</code> não é o resultado da autenticação, mas sim a <strong>matéria-prima</strong> necessária para ela, que depois é "promovida" a ser a representação oficial da identidade do usuário na sessão.
</p>

<p><strong>Resumo</strong></p>
<table>
  <thead>
    <tr>
      <th>Momento</th>
      <th>Papel do <code>UserDetails</code></th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Antes</strong> da Autenticação</td>
      <td>Não existe ainda.</td>
    </tr>
    <tr>
      <td><strong>Durante</strong> a Autenticação</td>
      <td>É criado pelo <code>UserDetailsService</code> para servir como a fonte de dados para a validação. Contém os dados do banco.</td>
    </tr>
    <tr>
      <td><strong>Após</strong> a Autenticação</td>
      <td>Ele se torna o <code>principal</code> dentro do objeto <code>Authentication</code>. Ele representa a identidade do usuário logado.</td>
    </tr>
  </tbody>
</table>

<h3>4.3. <code>DaoAuthenticationProvider</code></h3>

<p>
Foi dito, anteriormente, que o Spring Security delega o acesso aos dados para o <code>UserDetailsService</code>, e a parte do Spring Security responsável por essa delegação é a <code>DaoAuthenticationProvider</code>. 
</p>
<p>
<strong>DAO</strong> significa <strong>data access object</strong>, isto é, trata-se de um objeto responsável pelo acesso aos dados. Porém, esse provider delega o serviço ao <code>UserDetailsService</code>, conforme já vimos.
</p>

<h3>4.4. Exemplo de fluxo de autenticação do tipo username / password</h3>

<ol>
<li>
Um usuário envia um username e um password.
</li>
<br/>
<li>
Um controller intercepta a requisição e extrai essas duas informações. No caso da implementação padrão com <code>formLogin()</code>, a interceptação é feita por meio de um filtro (<code>UsernamePasswordAuthenticationFilter</code>).
</li>
<br/>
<li>
Uma <code>Authentication</code> (sua implementação: <code>UsernamePasswordAuthenticationToken</code>) é criada. Neste ponto, a <code>Authentication</code> possui como <code>principal</code> o username e como <code>credentials</code> o password, e está marcada como não autenticada.
</li>
<br/>
<li>
O token <code>UsernamePasswordAuthenticationToken</code> chega ao <code>AuthenticationManager</code>(sua implementação <code>ProviderManager</code>), que o delega ao <code>DaoAuthenticationProvider</code> (porque ele <em>"supports"</em> esse tipo de token). Lembre-se que o <code>ProviderManager</code> "pergunta" a cada <code>AuthenticationProvider</code> de sua lista qual é o responsável por lidar com o token recebido (que é um <code>Authentication</code>); e ele faz isso por meio do método <code>boolean supports(Class<?> authentication)</code>.
</li>
<br/>
<li>
O <code>DaoAuthenticationProvider</code> recebe o token e extrai o <code>username</code>.
</li>
<br/>
<li>
Agora ele precisa dos dados reais do usuário para comparar. Ele então chama o <code>userDetailsService.loadUserByUsername(username)</code>.
</li>
<br/>
<li>
Seu <code>UserDetailsService</code> vai ao banco de dados, encontra o usuário e retorna um objeto <code>UserDetails</code> com o username, senha codificada e permissões.
</li>
<br/>
<li>
O <code>DaoAuthenticationProvider</code> agora tem tudo o que precisa: a senha enviada pelo usuário (do token original) e o <code>UserDetails</code> (com a senha real codificada e as permissões).
</li>
<br/>
<li>
Ele usa o <code>PasswordEncoder</code> para comparar as senhas.
</li>
<br/>
<li>
Se bater, ele retorna um novo <code>UsernamePasswordAuthenticationToken</code>, mas agora o principal é o objeto <code>UserDetails</code>, <code>credentials</code> é <code>null</code> e as <code>authorities</code> estão populadas. Este token está marcado como autenticado.
</li>
</ol>

<p>
Dando uma olhada no antes e depois do token:
</p>

<table>
  <thead>
    <tr>
      <th>Característica</th>
      <th>Token ANTES da Autenticação</th>
      <th>Token APÓS a Autenticação</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><code>getPrincipal()</code></td>
      <td>Retorna o String do username.</td>
      <td>Retorna o objeto <code>UserDetails</code> completo.</td>
    </tr>
    <tr>
      <td><code>getCredentials()</code></td>
      <td>Retorna o String da senha.</td>
      <td>Retorna <code>null</code>.</td>
    </tr>
    <tr>
      <td><code>getAuthorities()</code></td>
      <td>Coleção vazia.</td>
      <td>Coleção de <code>GrantedAuthority</code> do usuário.</td>
    </tr>
    <tr>
      <td><code>isAuthenticated()</code></td>
      <td><code>false</code></td>
      <td><code>true</code></td>
    </tr>
  </tbody>
</table>