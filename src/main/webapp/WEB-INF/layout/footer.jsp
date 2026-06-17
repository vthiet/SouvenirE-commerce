<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<fmt:setLocale value="${requestScope.siteLocale}" scope="request"/>
<fmt:setBundle basename="messages" scope="request"/>
<c:set var="footerSiteName" value="${not empty settings.site_name ? settings.site_name : 'INOLA Souvenir'}"/>

<footer class="site-footer">
    <div class="layout-shell footer-main">
        <section class="footer-brand">
            <a href="${pageContext.request.contextPath}/home"
               class="footer-logo"
               aria-label="<fmt:message key='footer.home'/>">
                <c:choose>
                    <c:when test="${not empty settings.site_logo_url}">
                        <img src="${settings.site_logo_url}"
                             alt="<c:out value='${footerSiteName}'/>">
                    </c:when>
                    <c:otherwise>
                        <img src="${pageContext.request.contextPath}/assets/images/logo.png"
                             alt="<c:out value='${footerSiteName}'/>">
                    </c:otherwise>
                </c:choose>
            </a>

            <p>
                <c:choose>
                    <c:when test="${not empty settings.meta_description}">
                        <c:out value="${settings.meta_description}"/>
                    </c:when>
                    <c:otherwise>
                        <fmt:message key="footer.default_description"/>
                    </c:otherwise>
                </c:choose>
            </p>

            <div class="footer-social">
                <c:if test="${not empty settings.social_facebook}">
                    <a href="${settings.social_facebook}"
                       aria-label="Facebook"
                       target="_blank"
                       rel="noopener">
                        <i class="fa-brands fa-facebook-f"></i>
                    </a>
                </c:if>

                <c:if test="${not empty settings.social_instagram}">
                    <a href="${settings.social_instagram}"
                       aria-label="Instagram"
                       target="_blank"
                       rel="noopener">
                        <i class="fa-brands fa-instagram"></i>
                    </a>
                </c:if>

                <a href="#" aria-label="TikTok">
                    <i class="fa-brands fa-tiktok"></i>
                </a>

                <a href="#" aria-label="YouTube">
                    <i class="fa-brands fa-youtube"></i>
                </a>
            </div>
        </section>

        <section class="footer-col">
            <h2><fmt:message key="footer.shop"/></h2>
            <a href="${pageContext.request.contextPath}/home"><fmt:message key="footer.home"/></a>
            <a href="${pageContext.request.contextPath}/category"><fmt:message key="footer.categories"/></a>
            <a href="${pageContext.request.contextPath}/cart"><fmt:message key="footer.cart"/></a>
            <a href="${pageContext.request.contextPath}/user/orders"><fmt:message key="footer.orders"/></a>
        </section>

        <section class="footer-col">
            <h2><fmt:message key="footer.support"/></h2>
            <a href="#"><fmt:message key="footer.return_policy"/></a>
            <a href="#"><fmt:message key="footer.shipping_methods"/></a>
            <a href="#"><fmt:message key="footer.payment_guide"/></a>
            <a href="#"><fmt:message key="footer.contact"/></a>
        </section>

        <section class="footer-col footer-payments">
            <h2><fmt:message key="footer.payment"/></h2>

            <div class="footer-icon-grid">
                <c:if test="${settings.payment_card == 'true'}">
                    <span>
                        <img src="${pageContext.request.contextPath}/assets/images/Payment/visa.jpg"
                             alt="Visa">
                    </span>

                    <span>
                        <img src="${pageContext.request.contextPath}/assets/images/Payment/mastercard.png"
                             alt="Mastercard">
                    </span>
                </c:if>

                <c:if test="${settings.payment_momo == 'true'}">
                    <span>
                        <img src="${pageContext.request.contextPath}/assets/images/Payment/momo.webp"
                             alt="MoMo">
                    </span>
                </c:if>

                <c:if test="${settings.payment_vnpay == 'true'}">
                    <span>
                        <img src="${pageContext.request.contextPath}/assets/images/Payment/vnpay.webp"
                             alt="VNPAY">
                    </span>
                </c:if>
            </div>

            <h2 class="footer-subtitle"><fmt:message key="footer.shipping"/></h2>

            <div class="footer-icon-grid">
                <c:if test="${settings.shipping_ghtk == 'true'}">
                    <span>
                        <img src="${pageContext.request.contextPath}/assets/images/Transport/ghtk.webp"
                             alt="GHTK">
                    </span>
                </c:if>

                <c:if test="${settings.shipping_ghn == 'true'}">
                    <span>
                        <img src="${pageContext.request.contextPath}/assets/images/Transport/ghn.png"
                             alt="GHN">
                    </span>
                </c:if>

                <c:if test="${settings.shipping_jnt == 'true'}">
                    <span>
                        <img src="${pageContext.request.contextPath}/assets/images/Transport/jnt.webp"
                             alt="J&T Express">
                    </span>
                </c:if>
            </div>
        </section>
    </div>

    <div class="footer-bottom">
        <div class="layout-shell footer-bottom__inner">
            <span>
                <fmt:message key="footer.address_label"/>
                <c:choose>
                    <c:when test="${not empty settings.site_address}">
                        ${settings.site_address}
                    </c:when>
                    <c:otherwise>
                        <fmt:message key="footer.default_address"/>
                    </c:otherwise>
                </c:choose>
            </span>

            <span>
                <fmt:message key="footer.phone_label"/>
                <c:choose>
                    <c:when test="${not empty settings.site_phone}">
                        ${settings.site_phone}
                    </c:when>
                    <c:otherwise>
                        <fmt:message key="footer.default_phone"/>
                    </c:otherwise>
                </c:choose>
            </span>

            <span>
                <fmt:message key="footer.email_label"/>
                <a href="mailto:${settings.site_email}">
                    <c:choose>
                        <c:when test="${not empty settings.site_email}">
                            ${settings.site_email}
                        </c:when>
                        <c:otherwise>
                            <fmt:message key="footer.default_email"/>
                        </c:otherwise>
                    </c:choose>
                </a>
            </span>

            <span>
                <fmt:message key="footer.copyright">
                    <fmt:param value="${footerSiteName}"/>
                </fmt:message>
            </span>
        </div>
    </div>
</footer>
