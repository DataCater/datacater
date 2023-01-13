{{/*
Expand the name of the chart.
*/}}
{{- define "datacater.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "datacater.fullname" -}}
{{- .Chart.Name | trunc 63 }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "datacater.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "datacater.labels" -}}
{{ include "datacater.partOfLabel" . }}
{{ include "datacater.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
{{- if .Values.helmLabels.enabled }}
helm.sh/chart: {{ include "datacater.chart" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "datacater.selectorLabels" -}}
app.kubernetes.io/name: {{ include "datacater.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Part of label
*/}}
{{- define "datacater.partOfLabel" -}}
app.kubernetes.io/part-of: {{ include "datacater.chart" . }}
{{- end }}

{{/*
Common labels for ui
*/}}
{{- define "datacater.labelsUi" -}}
{{ include "datacater.partOfLabel" . }}
{{ include "datacater.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
{{- if .Values.helmLabels.enabled }}
helm.sh/chart: {{ include "datacater.chart" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}
{{- end }}

{{/*
Selector labels ui
*/}}
{{- define "datacater.selectorLabelsUi" -}}
app.kubernetes.io/name: "datacater-ui"
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Common labels for python-runner
*/}}
{{- define "datacater.labelsPythonRunner" -}}
{{ include "datacater.partOfLabel" . }}
{{ include "datacater.selectorLabelsPythonRunner" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
{{- if .Values.helmLabels.enabled }}
helm.sh/chart: {{ include "datacater.chart" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}
{{- end }}

{{/*
Selector labels python-runner
*/}}
{{- define "datacater.selectorLabelsPythonRunner" -}}
app.kubernetes.io/name: "python-runner"
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "datacater.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "datacater.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}
