from django.urls import path

from .analysis.views import analysis
from .analysis_validation.views import analysis_validation
from .decomposition.views import decomposition
from .decomposition_validation.views import decomposition_validation
from .transform.views import transform
from .transform_validation.views import transform_validation

urlpatterns = [
    path("analysis", analysis),
    path("analysis-validation", analysis_validation),
    path("decomposition", decomposition),
    path("decomposition-validation", decomposition_validation),
    path("transform", transform),
    path("transform-validation", transform_validation),
]