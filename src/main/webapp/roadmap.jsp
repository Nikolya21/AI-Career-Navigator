<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Roadmap - AI Career Navigator</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/roadmap.css">
</head>
<body>
<div class="container">
    <header>
        <h1>🗺️ Персональный Roadmap</h1>
        <nav class="breadcrumb">
            <a href="${pageContext.request.contextPath}/index.jsp">Главная</a> > Roadmap
        </nav>
    </header>

    <main>
        <!-- Блок выбора цели -->
        <div class="card">
            <h2>🎯 Выберите вашу карьерную цель</h2>
            <form action="roadmap" method="post" class="goal-form">
                <input type="hidden" name="action" value="generate">

                <div class="form-group">
                    <label for="currentLevel">Текущий уровень:</label>
                    <select id="currentLevel" name="currentLevel" required>
                        <option value="Junior">Junior Developer</option>
                        <option value="Middle" selected>Middle Developer</option>
                        <option value="Senior">Senior Developer</option>
                        <option value="Team Lead">Team Lead</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="targetLevel">Целевой уровень:</label>
                    <select id="targetLevel" name="targetLevel" required>
                        <option value="Middle">Middle Developer</option>
                        <option value="Senior" selected>Senior Developer</option>
                        <option value="Team Lead">Team Lead</option>
                        <option value="Architect">Software Architect</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="technologyStack">Технологический стек:</label>
                    <select id="technologyStack" name="technologyStack" required>
                        <option value="Java">Java Backend</option>
                        <option value="Frontend">JavaScript/Frontend</option>
                        <option value="Python">Python/Data Science</option>
                        <option value="DevOps">DevOps/Cloud</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="timeframe">Планируемый срок (месяцев):</label>
                    <input type="number" id="timeframe" name="timeframe"
                           value="12" min="3" max="36" required>
                </div>

                <button type="submit" class="btn-primary">🚀 Сгенерировать Roadmap</button>
            </form>
        </div>

        <!-- Блок сгенерированного roadmap -->
        <c:if test="${not empty roadmap}">
            <div class="card roadmap-card">
                <h2>📊 Ваш персональный план развития</h2>

                <div class="roadmap-header">
                    <div class="roadmap-info">
                        <h3>${roadmap.title}</h3>
                        <p><strong>Цель:</strong> ${roadmap.currentLevel} → ${roadmap.targetLevel}</p>
                        <p><strong>Срок:</strong> ${roadmap.timeframe} месяцев</p>
                        <p><strong>Технологии:</strong> ${roadmap.technologyStack}</p>
                    </div>
                    <div class="progress-section">
                        <div class="progress-bar">
                            <div class="progress" style="width: ${roadmap.progress}%"></div>
                        </div>
                        <span>${roadmap.progress}% выполнено</span>
                    </div>
                </div>

                <!-- Этапы roadmap -->
                <div class="roadmap-stages">
                    <c:forEach var="stage" items="${roadmap.stages}" varStatus="status">
                        <div class="stage ${stage.completed ? 'completed' : ''} ${stage.current ? 'current' : ''}">
                            <div class="stage-header">
                                <span class="stage-number">${status.index + 1}</span>
                                <h4>${stage.title}</h4>
                                <span class="stage-duration">${stage.duration} мес.</span>
                            </div>
                            <div class="stage-content">
                                <p>${stage.description}</p>

                                <c:if test="${not empty stage.skills}">
                                    <div class="skills-list">
                                        <strong>Навыки для изучения:</strong>
                                        <div class="skills">
                                            <c:forEach var="skill" items="${stage.skills}">
                                                <span class="skill-tag">${skill}</span>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </c:if>

                                <c:if test="${not empty stage.courses}">
                                    <div class="courses-list">
                                        <strong>Рекомендуемые курсы:</strong>
                                        <ul>
                                            <c:forEach var="course" items="${stage.courses}">
                                                <li>${course}</li>
                                            </c:forEach>
                                        </ul>
                                    </div>
                                </c:if>

                                <c:if test="${not empty stage.projects}">
                                    <div class="projects-list">
                                        <strong>Практические проекты:</strong>
                                        <ul>
                                            <c:forEach var="project" items="${stage.projects}">
                                                <li>${project}</li>
                                            </c:forEach>
                                        </ul>
                                    </div>
                                </c:if>

                                <div class="stage-actions">
                                    <c:if test="${stage.current}">
                                        <button class="btn-mark-complete"
                                                onclick="markStageComplete(${status.index})">
                                            ✅ Отметить выполненным
                                        </button>
                                    </c:if>
                                    <c:if test="${stage.completed}">
                                        <span class="completed-badge">✅ Выполнено</span>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <!-- Метрики прогресса -->
                <div class="metrics">
                    <div class="metric-card">
                        <div class="metric-value">${roadmap.completedStages}</div>
                        <div class="metric-label">Этапов выполнено</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">${roadmap.totalSkills}</div>
                        <div class="metric-label">Навыков для освоения</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">${roadmap.estimatedSalary}%</div>
                        <div class="metric-label">Рост зарплаты</div>
                    </div>
                    <div class="metric-card">
                        <div class="metric-value">${roadmap.daysRemaining}</div>
                        <div class="metric-label">Дней до цели</div>
                    </div>
                </div>
            </div>
        </c:if>

        <!-- Шаблоны roadmap -->
        <div class="card templates-card">
            <h2>📋 Готовые шаблоны развития</h2>
            <div class="templates-grid">
                <div class="template" onclick="loadTemplate('java_junior_to_middle')">
                    <h4>Java Junior → Middle</h4>
                    <p>Освоение Spring Boot, SQL, паттернов проектирования</p>
                    <span class="template-duration">6-9 месяцев</span>
                </div>

                <div class="template" onclick="loadTemplate('java_middle_to_senior')">
                    <h4>Java Middle → Senior</h4>
                    <p>Microservices, Cloud, System Design, Leadership</p>
                    <span class="template-duration">12-18 месяцев</span>
                </div>

                <div class="template" onclick="loadTemplate('frontend_roadmap')">
                    <h4>Frontend Developer</h4>
                    <p>React, TypeScript, State Management, Testing</p>
                    <span class="template-duration">8-12 месяцев</span>
                </div>

                <div class="template" onclick="loadTemplate('devops_roadmap')">
                    <h4>DevOps Engineer</h4>
                    <p>Docker, Kubernetes, CI/CD, Cloud Platforms</p>
                    <span class="template-duration">10-15 месяцев</span>
                </div>
            </div>
        </div>
    </main>
</div>

<script src="${pageContext.request.contextPath}/js/roadmap.js"></script>
</body>
</html>